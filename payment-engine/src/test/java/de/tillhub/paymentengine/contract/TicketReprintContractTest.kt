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
import io.kotest.assertions.throwables.shouldThrow
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
class TicketReprintContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: TicketReprintContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TicketReprintContract(analytics)
    }

//    test("createIntent SPOS") {
//        val result = target.createIntent(
//            context,
//            Terminal.SPOS(
//                id = "s-pos",
//            )
//        )
//
//        result.action shouldBe "de.spayment.akzeptanz.REPRINT_TICKET"
//
//        verify {
//            analytics.logOperation(
//                "Operation: REPRINT_TICKET" +
//                        "\nTerminal.SPOS(" +
//                        "id=s-pos, " +
//                        "appId=TESTCLIENT, " +
//                        "saleConfig=CardSaleConfig(" +
//                        "applicationName=Tillhub GO, " +
//                        "operatorId=ah, " +
//                        "saleId=registerProvider, " +
//                        "pin=333333, " +
//                        "poiId=66000001, " +
//                        "poiSerialNumber=" +
//                        "), " +
//                        "currencyCode=EUR" +
//                        ")"
//            )
//        }
//    }

    test("createIntent OPI") {
        val result = shouldThrow<UnsupportedOperationException> {
            target.createIntent(context, Terminal.OPI())
        }

        verify(inverse = true) {
            analytics.logOperation(any())
        }

        result.message shouldBe "Ticket reprint is not supported by this terminal"
    }

    test("createIntent ZVT") {
        val result = shouldThrow<UnsupportedOperationException> {
            target.createIntent(context, Terminal.ZVT())
        }

        verify(inverse = true) {
            analytics.logOperation(any())
        }

        result.message shouldBe "Ticket reprint is not supported by this terminal"
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.RESULT_STATE, "Success")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_RESULT, "ACCEPTED")
            putExtra(SPOSKey.ResultExtra.TERMINAL_ID, "terminal_id")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_DATA, "transaction_data")
            putExtra(SPOSKey.ResultExtra.CARD_CIRCUIT, "card_circuit")
            putExtra(SPOSKey.ResultExtra.CARD_PAN, "card_pan")
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.TicketReprint.Success>()
    }
})