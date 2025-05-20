package de.tillhub.paymentengine.spos

import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import de.tillhub.paymentengine.spos.data.SPOSTransactionType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSIntentFactoryTest : FunSpec({

    test("createConnectIntent") {
        val intent = SPOSIntentFactory.createConnectIntent(TERMINAL)
        intent.action shouldBe SPOSKey.Action.CONNECT_ACTION
        intent.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe TERMINAL.appId
    }

    test("createDisconnectIntent") {
        val intent = SPOSIntentFactory.createDisconnectIntent(TERMINAL)
        intent.action shouldBe SPOSKey.Action.DISCONNECT_ACTION
        intent.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe TERMINAL.appId
    }

    test("createPaymentIntent") {
        val intent = SPOSIntentFactory.createPaymentIntent(
            PaymentRequest(
                config = TERMINAL,
                transactionId = "transactionId",
                amount = 100.toBigDecimal(),
                tip = 10.toBigDecimal(),
                currency = ISOAlphaCurrency("EUR")
            )
        )

        intent.action shouldBe SPOSKey.Action.TRANSACTION_ACTION
        intent.extras?.getString(
            SPOSKey.Extra.TRANSACTION_TYPE
        ) shouldBe SPOSTransactionType.PAYMENT.value
        intent.extras?.getString(SPOSKey.Extra.CURRENCY_ISO) shouldBe "EUR"
        intent.extras?.getString(SPOSKey.Extra.AMOUNT) shouldBe "100"
        intent.extras?.getString(SPOSKey.Extra.TIP_AMOUNT) shouldBe "10"
        intent.extras?.getString(SPOSKey.Extra.TRANSACTION_ID) shouldBe "transactionId"
        intent.extras?.getString(SPOSKey.Extra.TAX_AMOUNT) shouldBe "000"
    }

    test("createPaymentRefundIntent") {
        val intent = SPOSIntentFactory.createPaymentRefundIntent(
            RefundRequest(
                config = TERMINAL,
                transactionId = "transactionId",
                amount = 100.toBigDecimal(),
                currency = ISOAlphaCurrency("EUR")
            )
        )

        intent.action shouldBe SPOSKey.Action.TRANSACTION_ACTION
        intent.extras?.getString(
            SPOSKey.Extra.TRANSACTION_TYPE
        ) shouldBe SPOSTransactionType.PAYMENT_REFUND.value
        intent.extras?.getString(SPOSKey.Extra.CURRENCY_ISO) shouldBe "EUR"
        intent.extras?.getString(SPOSKey.Extra.AMOUNT) shouldBe "100"
        intent.extras?.getString(SPOSKey.Extra.TRANSACTION_ID) shouldBe "transactionId"
        intent.extras?.getString(SPOSKey.Extra.TAX_AMOUNT) shouldBe "000"
    }

    test("createPaymentReversalIntent") {
        val intent = SPOSIntentFactory.createPaymentReversalIntent(
            ReversalRequest(
                config = TERMINAL,
                transactionId = "transactionId",
                amount = 100.toBigDecimal(),
                tip = 10.toBigDecimal(),
                currency = ISOAlphaCurrency("EUR"),
                receiptNo = "receiptNo"
            )
        )

        intent.action shouldBe SPOSKey.Action.TRANSACTION_ACTION
        intent.extras?.getString(
            SPOSKey.Extra.TRANSACTION_TYPE
        ) shouldBe SPOSTransactionType.PAYMENT_REVERSAL.value
        intent.extras?.getString(SPOSKey.Extra.CURRENCY_ISO) shouldBe "EUR"
        intent.extras?.getString(SPOSKey.Extra.AMOUNT) shouldBe "100"
        intent.extras?.getString(SPOSKey.Extra.TIP_AMOUNT) shouldBe "10"
        intent.extras?.getString(SPOSKey.Extra.TRANSACTION_ID) shouldBe "transactionId"
        intent.extras?.getString(SPOSKey.Extra.TAX_AMOUNT) shouldBe "000"
        intent.extras?.getString(SPOSKey.Extra.TRANSACTION_DATA) shouldBe "receiptNo"
    }

    test("createRecoveryIntent") {
        val intent = SPOSIntentFactory.createRecoveryIntent()

        intent.action shouldBe SPOSKey.Action.RECOVERY_ACTION
    }

    test("createTicketReprintIntent") {
        val intent = SPOSIntentFactory.createTicketReprintIntent()

        intent.action shouldBe SPOSKey.Action.TICKET_REPRINT_ACTION
    }

    test("createReconciliationIntent") {
        val intent = SPOSIntentFactory.createReconciliationIntent()

        intent.action shouldBe SPOSKey.Action.RECONCILIATION_ACTION
    }
}) {
    companion object {
        private val TERMINAL = SPOSTerminal()
    }
}
