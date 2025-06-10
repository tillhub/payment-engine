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
class TerminalDisconnectContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: TerminalDisconnectContract

    val terminal: Terminal = TestTerminal("external")

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TerminalDisconnectContract(analytics)
    }

    test("createIntent") {
        every { terminal.contract.disconnectIntent(any(), any()) } returns Intent("DISCONNECT")
        val result = target.createIntent(context, terminal)

        verify {
            terminal.contract.disconnectIntent(context, terminal)
            analytics.logOperation(
                "Operation: TERMINAL_DISCONNECT\n" +
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
        result.action shouldBe "DISCONNECT"
    }

    test("parseResult: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Login.Disconnected(
                    date = mockk(),
                )
            )
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Disconnected>()

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