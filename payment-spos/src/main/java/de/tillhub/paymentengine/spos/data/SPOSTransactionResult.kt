package de.tillhub.paymentengine.spos.data

internal enum class SPOSTransactionResult(val value: String) {
    ACCEPTED("ACCEPTED"),
    FAILED("FAILED"),
    UNKNOWN("unknown");

    companion object {
        fun find(type: String): SPOSTransactionResult =
            SPOSTransactionResult.entries.find { it.value == type } ?: UNKNOWN
    }
}