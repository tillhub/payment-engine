package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.BundleCompat
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class TerminalConnectContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: TerminalConnectContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TerminalConnectContract(analytics)
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            SPOS
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.S_SWITCH_CONNECT"
        result.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe "TESTCLIENT"

        verify {
            analytics.logOperation(
                "Operation: TERMINAL_CONNECT" +
                        "\nTerminal.SPOS(" +
                        "id=s-pos, " +
                        "appId=TESTCLIENT, " +
                        "saleConfig=CardSaleConfig(" +
                        "applicationName=Tillhub GO, " +
                        "operatorId=ah, " +
                        "saleId=registerProvider, " +
                        "pin=333333, " +
                        "poiId=66000001, " +
                        "poiSerialNumber=" +
                        "), " +
                        "currencyCode=EUR" +
                        ")"
            )
        }
    }

    test("createIntent OPI") {
        val result = target.createIntent(
            context,
            OPI
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPILoginActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            Terminal.OPI::class.java
        ) shouldBe PaymentResultContractTest.OPI
    }

    test("createIntent ZVT") {
        val result = target.createIntent(
            context,
            ZVT
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.TerminalLoginActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            Terminal.ZVT::class.java
        ) shouldBe ZVT
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Connected>()

        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT OK"
            )
        }
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.ERROR, "CARD_PAYMENT_NOT_ONBOARDED")
        }

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()

        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT CANCELED\nExtras {\n" +
                        "ERROR = CARD_PAYMENT_NOT_ONBOARDED\n}"
            )
        }
    }

    test("parseResult OPI + ZVT: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Login.Connected(
                    date = mockk(),
                    rawData = "rawData",
                    terminalType = Terminal.OPI.TYPE,
                    terminalId = "terminalId"
                )
            )
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Connected>()
    }

    test("parseResult OPI + ZVT: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Canceled>()
    }
}) {
    companion object {
        val OPI = Terminal.OPI(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
        val ZVT = Terminal.ZVT(
            id = "zvt",
            ipAddress = "127.0.0.1",
            port = 20007,
        )
        val SPOS = Terminal.SPOS(
            id = "s-pos",
        )
    }
}
