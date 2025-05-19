package de.tillhub.paymentengine

import android.os.Bundle
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal

@Suppress("TooManyFunctions")
internal object AnalyticsMessageFactory {
    fun createPaymentOperation(input: PaymentRequest) = "Operation: CARD_PAYMENT(" +
            "amount: ${input.amount}, " +
            "tip: ${input.tip}, " +
            "currency: ${input.currency})" +
            "\n${input.config}"

    fun createRefundOperation(input: RefundRequest) = "Operation: PARTIAL_REFUND(" +
            "amount: ${input.amount}, " +
            "currency: ${input.currency})" +
            "\n${input.config}"

    fun createReversalOperation(input: ReversalRequest) = "Operation: CARD_PAYMENT_REVERSAL(" +
            "stan: ${input.receiptNo})" +
            "\n${input.config}"

    fun createReconciliationOperation(input: Terminal) = "Operation: RECONCILIATION\n$input"

    fun createConnectOperation(input: Terminal) = "Operation: TERMINAL_CONNECT\n$input"

    fun createDisconnectOperation(input: Terminal) = "Operation: TERMINAL_DISCONNECT\n$input"

    fun createRecoveryOperation(input: Terminal) = "Operation: TERMINAL_RECOVERY\n$input"

    fun createTicketReprintOperation(input: Terminal) = "Operation: REPRINT_TICKET\n$input"

    fun createResultOk(extras: Bundle?) = "$RESPONSE_RESULT_OK\n${extras?.toRawData()}"

    private fun Bundle.toRawData(): String {
        val builder = StringBuilder()
        builder.appendLine("Extras {")

        keySet().forEach {
            builder.appendLine("$it = ${getString(it)}")
        }

        builder.append("}")
        return builder.toString()
    }

    const val RESPONSE_RESULT_OK = "RESPONSE: RESULT OK"
}