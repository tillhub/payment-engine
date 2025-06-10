package de.tillhub.paymentengine.spos.data

internal enum class SPOSTransactionType(val value: String) {
    PAYMENT("CardPayment"),
    PAYMENT_REFUND("PaymentRefund"),
    PAYMENT_REVERSAL("PaymentReversal"),
    UNKNOWN("unknown");

    companion object {
        fun find(type: String): SPOSTransactionType =
            SPOSTransactionType.entries.find { it.value == type } ?: UNKNOWN
    }
}