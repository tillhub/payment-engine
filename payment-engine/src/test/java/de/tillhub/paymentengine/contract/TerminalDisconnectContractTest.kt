package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
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
class TerminalDisconnectContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: TerminalDisconnectContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TerminalDisconnectContract(analytics)
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            SPOS
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.S_SWITCH_DISCONNECT"
        result.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe "TESTCLIENT"

        verify {
            analytics.logOperation(
                "Operation: TERMINAL_DISCONNECT" +
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

    test("createIntent OPI + ZVT") {
        try {
            target.createIntent(
                context,
                OPI
            )
        } catch (e: Exception) {
            e.shouldBeInstanceOf<UnsupportedOperationException>()
            e.message shouldBe "Disconnect only supported for S-POS terminals"
        }
        verify(inverse = true) {
            analytics.logOperation(any())
        }
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Success.SPOS>()
        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT OK"
            )
        }
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Canceled>()
        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT CANCELED"
            )
        }
    }
}) {
    companion object {
        val OPI = Terminal.OPI(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
        val SPOS = Terminal.SPOS(
            id = "s-pos",
        )
    }
}