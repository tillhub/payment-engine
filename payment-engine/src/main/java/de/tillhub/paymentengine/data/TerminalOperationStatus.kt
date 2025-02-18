package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.Instant
import java.util.Objects

@Parcelize
sealed class TerminalOperationStatus : Parcelable {
    data object Waiting : TerminalOperationStatus()
    data object Canceled : TerminalOperationStatus()

    @Parcelize
    sealed class Pending : TerminalOperationStatus() {
        class Payment(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending() {

            override fun equals(other: Any?) = other is Payment &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Pending.Payment(amount=$amount, currency=$currency)"
        }
        class Reversal(val receiptNo: String) : Pending() {

            override fun equals(other: Any?) = other is Reversal &&
                    receiptNo == other.receiptNo

            override fun hashCode() = Objects.hash(receiptNo)
            override fun toString() = "Pending.Reversal(receiptNo=$receiptNo)"
        }
        class Refund(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending() {

            override fun equals(other: Any?) = other is Refund &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Pending.Refund(amount=$amount, currency=$currency)"
        }
        data object Reconciliation : Pending()

        data object Connecting : Pending()
        data object Disconnecting : Pending()
        data object Recovery : Pending()
    }

    @Parcelize
    sealed class Success : TerminalOperationStatus() {
        abstract val date: Instant
        abstract val customerReceipt: String
        abstract val merchantReceipt: String
        abstract val rawData: String
        abstract val data: TransactionData?

        class ZVT(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: TransactionData?
        ) : Success()

        class OPI(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: TransactionData?
        ) : Success()

        class SPOS(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: TransactionData?
        ) : Success()

        override fun toString() = "Success(" +
                "date=$date, " +
                "customerReceipt=$customerReceipt, " +
                "merchantReceipt=$merchantReceipt, " +
                "rawData=$rawData, " +
                "data=$data" +
                ")"

        override fun equals(other: Any?) = when (this) {
            is OPI -> other is OPI && equalsVals(other)
            is ZVT -> other is ZVT && equalsVals(other)
            is SPOS -> other is SPOS && equalsVals(other)
        }

        override fun hashCode() = Objects.hash(
            date,
            customerReceipt,
            merchantReceipt,
            rawData,
            data
        )

        private fun equalsVals(other: Success) =
            date == other.date &&
                customerReceipt == other.customerReceipt &&
                merchantReceipt == other.merchantReceipt &&
                rawData == other.rawData &&
                data == other.data
    }

    @Parcelize
    sealed class Error : TerminalOperationStatus() {
        abstract val date: Instant
        abstract val customerReceipt: String
        abstract val merchantReceipt: String
        abstract val rawData: String
        abstract val data: TransactionData?
        abstract val resultCode: TransactionResultCode
        abstract val isRecoverable: Boolean

        class ZVT(
            override val date: Instant,
            override val customerReceipt: String = "",
            override val merchantReceipt: String = "",
            override val rawData: String = "",
            override val data: TransactionData? = null,
            override val resultCode: TransactionResultCode,
            override val isRecoverable: Boolean = false
        ) : Error()

        class OPI(
            override val date: Instant,
            override val customerReceipt: String = "",
            override val merchantReceipt: String = "",
            override val rawData: String = "",
            override val data: TransactionData? = null,
            override val resultCode: TransactionResultCode,
            override val isRecoverable: Boolean = false
        ) : Error()

        class SPOS(
            override val date: Instant,
            override val customerReceipt: String = "",
            override val merchantReceipt: String = "",
            override val rawData: String = "",
            override val data: TransactionData? = null,
            override val resultCode: TransactionResultCode,
            override val isRecoverable: Boolean
        ) : Error()

        override fun equals(other: Any?) = other is Error &&
                date == other.date &&
                customerReceipt == other.customerReceipt &&
                merchantReceipt == other.merchantReceipt &&
                rawData == other.rawData &&
                data == other.data &&
                resultCode == other.resultCode &&
                isRecoverable == other.isRecoverable

        override fun hashCode() = Objects.hash(
            date,
            customerReceipt,
            merchantReceipt,
            rawData,
            data,
            resultCode,
            isRecoverable
        )

        override fun toString() = "Error(" +
                "date=$date, " +
                "customerReceipt=$customerReceipt, " +
                "merchantReceipt=$merchantReceipt, " +
                "rawData=$rawData, " +
                "data=$data, " +
                "resultCode=$resultCode" +
                "isRecoverable=$isRecoverable" +
                ")"
    }
}
