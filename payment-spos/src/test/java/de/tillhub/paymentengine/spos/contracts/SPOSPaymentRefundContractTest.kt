package de.tillhub.paymentengine.spos.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSPaymentRefundContractTest : FunSpec({
    lateinit var context: Context
    lateinit var target: SPOSPaymentRefundContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())

        target = SPOSPaymentRefundContract()
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            RefundRequest(
                SPOS,
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.TRANSACTION"
        result.extras?.getString(SPOSKey.Extra.TRANSACTION_TYPE) shouldBe "PaymentRefund"
        result.extras?.getString(SPOSKey.Extra.CURRENCY_ISO) shouldBe "EUR"
        result.extras?.getString(SPOSKey.Extra.AMOUNT) shouldBe "500"
        result.extras?.getString(SPOSKey.Extra.TRANSACTION_ID) shouldBe "UUID"
        result.extras?.getString(SPOSKey.Extra.TAX_AMOUNT) shouldBe "000"
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

        result.shouldBeInstanceOf<TerminalOperationStatus.Refund.Success>()
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.ERROR, "CARD_PAYMENT_NOT_ONBOARDED")
        }

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Refund.Error>()
    }
}) {
    companion object {
        val SPOS = SPOSTerminal(
            id = "s-pos",
        )
    }
}