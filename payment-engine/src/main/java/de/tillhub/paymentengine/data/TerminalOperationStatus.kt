package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.Instant

@Parcelize
sealed class TerminalOperationStatus : Parcelable {
    data object Waiting : TerminalOperationStatus()
    data object Canceled : TerminalOperationStatus()

    @Parcelize
    sealed class Pending : TerminalOperationStatus() {
        data class Payment(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending()
        data class Reversal(val receiptNo: String) : Pending()
        data class Refund(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending()
        data object Reconciliation : Pending()
    }

    @Parcelize
    sealed class Success : TerminalOperationStatus() {
        abstract val date: Instant
        abstract val customerReceipt: String
        abstract val merchantReceipt: String
        abstract val rawData: String

        data class ZVT(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            val data: LavegoTransactionData?
        ) : Success()
        // TODO("To be implemented in future")
//        data class OPI(
//            override val date: Instant,
//            override val customerReceipt: String,
//            override val merchantReceipt: String,
//            override val rawData: String
//        ) : Success()
    }

    @Parcelize
    sealed class Error : TerminalOperationStatus() {
        abstract val date: Instant
        abstract val customerReceipt: String
        abstract val merchantReceipt: String
        abstract val rawData: String

        data class ZVT(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            val data: LavegoTransactionData?,
            val resultCode: TransactionResultCode
        ) : Error()
        // TODO("To be implemented in future")
//        data class OPI(
//            override val date: Instant,
//            override val customerReceipt: String,
//            override val merchantReceipt: String,
//            override val rawData: String
//        ) : Error()
    }
}
