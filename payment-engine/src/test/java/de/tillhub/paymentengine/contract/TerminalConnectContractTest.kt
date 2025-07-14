package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.testing.TestTerminal
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

    val terminal: Terminal = TestTerminal("external")

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TerminalConnectContract(analytics)
    }

    test("createIntent") {
        every { terminal.contract.connectIntent(any(), any()) } returns Intent("CONNECT")
        val result = target.createIntent(context, terminal)

        verify {
            terminal.contract.connectIntent(context, terminal)
            analytics.logOperation(
                "Operation: TERMINAL_CONNECT\n" +
                    "Terminal.External(" +
                    "id=external, " +
                    "saleConfig=CardSaleConfig(" +
                    "applicationName=Tillhub GO, " +
                    "operatorId=ah, " +
                    "saleId=registerProvider, " +
                    "pin=333333, " +
                    "poiId=66000001, " +
                    "poiSerialNumber=" +
                    ")" +
                    ")"
            )
        }

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "CONNECT"
    }

    test("parseResult: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Login.Connected(
                    date = mockk(),
                    rawData = "rawData",
                    terminalType = "ZVT",
                    terminalId = "terminalId"
                )
            )
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Connected>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }

    test("parseResult: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Canceled>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }
})
