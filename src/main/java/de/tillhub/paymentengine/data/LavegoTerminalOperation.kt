package de.tillhub.paymentengine.data

import java.math.BigDecimal
import java.time.Instant

sealed class LavegoTerminalOperation {
    object Waiting : LavegoTerminalOperation()

    sealed class Pending : LavegoTerminalOperation() {
        data class Payment(val amount: BigDecimal) : Pending()
        data class Reversal(val receiptNo: String) : Pending()
        data class PartialRefund(val amount: BigDecimal) : Pending()
        object Reconciliation : Pending()
    }

    data class Failed(
        val date: Instant,
        val customerReceipt: String,
        val merchantReceipt: String,
        val rawData: String,
        val data: LavegoTransactionData?,
    ) : LavegoTerminalOperation()

    data class Success(
        val date: Instant,
        val customerReceipt: String,
        val merchantReceipt: String,
        val rawData: String,
        val data: LavegoTransactionData?,
    ) : LavegoTerminalOperation()
}
