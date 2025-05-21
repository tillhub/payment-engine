package de.tillhub.paymentengine.spos

import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSTransactionType
import de.tillhub.paymentengine.spos.data.SPOSTerminal

internal object SPOSIntentFactory {

    fun createConnectIntent(terminal: SPOSTerminal) =
        Intent(SPOSKey.Action.CONNECT_ACTION).apply {
            putExtra(SPOSKey.Extra.APP_ID, terminal.appId)
        }

    fun createDisconnectIntent(terminal: SPOSTerminal) =
        Intent(SPOSKey.Action.DISCONNECT_ACTION).apply {
            putExtra(SPOSKey.Extra.APP_ID, terminal.appId)
        }

    fun createPaymentIntent(request: PaymentRequest) =
        Intent(SPOSKey.Action.TRANSACTION_ACTION).apply {
            putExtra(SPOSKey.Extra.TRANSACTION_TYPE, SPOSTransactionType.PAYMENT.value)
            putExtra(SPOSKey.Extra.CURRENCY_ISO, request.currency.value)
            putExtra(SPOSKey.Extra.AMOUNT, request.amount.toString())
            putExtra(SPOSKey.Extra.TIP_AMOUNT, request.tip.toString())
            putExtra(SPOSKey.Extra.TRANSACTION_ID, request.transactionId)

            // As per documentation this field is required, but not used in S-POS
            // and is reserved for future use. Value can be set to "000".
            putExtra(SPOSKey.Extra.TAX_AMOUNT, "000")
        }

    fun createPaymentRefundIntent(request: RefundRequest) =
        Intent(SPOSKey.Action.TRANSACTION_ACTION).apply {
            putExtra(SPOSKey.Extra.TRANSACTION_TYPE, SPOSTransactionType.PAYMENT_REFUND.value)
            putExtra(SPOSKey.Extra.CURRENCY_ISO, request.currency.value)
            putExtra(SPOSKey.Extra.AMOUNT, request.amount.toString())
            putExtra(SPOSKey.Extra.TRANSACTION_ID, request.transactionId)

            // As per documentation this field is required, but not used in S-POS
            // and is reserved for future use. Value can be set to "000".
            putExtra(SPOSKey.Extra.TAX_AMOUNT, "000")
        }

    fun createPaymentReversalIntent(request: ReversalRequest) =
        Intent(SPOSKey.Action.TRANSACTION_ACTION).apply {
            putExtra(SPOSKey.Extra.TRANSACTION_TYPE, SPOSTransactionType.PAYMENT_REVERSAL.value)
            putExtra(SPOSKey.Extra.CURRENCY_ISO, request.currency.value)
            putExtra(SPOSKey.Extra.AMOUNT, request.amount.toString())
            putExtra(SPOSKey.Extra.TIP_AMOUNT, request.tip.toString())
            putExtra(SPOSKey.Extra.TRANSACTION_DATA, request.receiptNo)
            putExtra(SPOSKey.Extra.TRANSACTION_ID, request.transactionId)

            // As per documentation this field is required, but not used in S-POS
            // and is reserved for future use. Value can be set to "000".
            putExtra(SPOSKey.Extra.TAX_AMOUNT, "000")
        }

    fun createRecoveryIntent() =
        Intent(SPOSKey.Action.RECOVERY_ACTION)

    fun createTicketReprintIntent() =
        Intent(SPOSKey.Action.TICKET_REPRINT_ACTION)

    fun createReconciliationIntent() =
        Intent(SPOSKey.Action.RECONCILIATION_ACTION)
}