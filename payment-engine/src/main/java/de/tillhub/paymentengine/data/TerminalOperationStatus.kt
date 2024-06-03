package de.tillhub.paymentengine.data

import android.os.Parcelable
import de.tillhub.paymentengine.zvt.data.TransactionResultCode
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.Instant
import java.util.Objects

@Parcelize
sealed class TerminalOperationStatus : Parcelable, Comparable<TerminalOperationStatus> {
    data object Waiting : TerminalOperationStatus()
    data object Canceled : TerminalOperationStatus()

    @Parcelize
    sealed class Pending : TerminalOperationStatus() {
        class Payment(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending() {
            override fun compareTo(other: TerminalOperationStatus): Int = if (other is Payment) {
                amount.compareTo(other.amount) + currency.value.compareTo(other.currency.value)
            } else {
                -1
            }

            override fun equals(other: Any?) = other is Payment &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Pending.Payment(amount=$amount, currency=$currency)"
        }
        class Reversal(val receiptNo: String) : Pending() {
            override fun compareTo(other: TerminalOperationStatus): Int = if (other is Reversal) {
                receiptNo.compareTo(other.receiptNo)
            } else {
                -1
            }

            override fun equals(other: Any?) = other is Reversal &&
                    receiptNo == other.receiptNo

            override fun hashCode() = Objects.hash(receiptNo)
            override fun toString() = "Pending.Reversal(receiptNo=$receiptNo)"
        }
        class Refund(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Pending() {
            override fun compareTo(other: TerminalOperationStatus): Int = if (other is Refund) {
                amount.compareTo(other.amount) + currency.value.compareTo(other.currency.value)
            } else {
                -1
            }

            override fun equals(other: Any?) = other is Refund &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Pending.Refund(amount=$amount, currency=$currency)"
        }
        data object Reconciliation : Pending()
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

        override fun compareTo(other: TerminalOperationStatus): Int = if (other is Success) {
            date.compareTo(other.date) +
                    customerReceipt.compareTo(other.customerReceipt) +
                    merchantReceipt.compareTo(other.merchantReceipt) +
                    rawData.compareTo(other.rawData) +
                    if (data != null && other.data != null) {
                        data!!.compareTo(other.data!!)
                    } else if (data == null && other.data == null) {
                        0
                    } else {
                        -1
                    }
        } else {
            -1
        }

        override fun equals(other: Any?) = other is ZVT &&
                date == other.date &&
                customerReceipt == other.customerReceipt &&
                merchantReceipt == other.merchantReceipt &&
                rawData == other.rawData &&
                data == other.data

        override fun hashCode() = Objects.hash(
            date,
            customerReceipt,
            merchantReceipt,
            rawData,
            data
        )
        override fun toString() = "Success.ZVT(" +
                "date=$date, " +
                "customerReceipt=$customerReceipt, " +
                "merchantReceipt=$merchantReceipt, " +
                "rawData=$rawData, " +
                "data=$data" +
                ")"
    }

    @Parcelize
    sealed class Error : TerminalOperationStatus() {
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
            override val data: TransactionData?,
            val resultCode: TransactionResultCode
        ) : Error() {
            override fun compareTo(other: TerminalOperationStatus): Int = if (other is ZVT) {
                date.compareTo(other.date) +
                        customerReceipt.compareTo(other.customerReceipt) +
                        merchantReceipt.compareTo(other.merchantReceipt) +
                        rawData.compareTo(other.rawData) +
                        resultCode.compareTo(other.resultCode) +
                        if (data != null && other.data != null) {
                            data.compareTo(other.data)
                        } else if (data == null && other.data == null) {
                            0
                        } else {
                            -1
                        }
            } else {
                -1
            }

            override fun equals(other: Any?) = other is ZVT &&
                    date == other.date &&
                    customerReceipt == other.customerReceipt &&
                    merchantReceipt == other.merchantReceipt &&
                    rawData == other.rawData &&
                    data == other.data &&
                    resultCode == other.resultCode

            override fun hashCode() = Objects.hash(
                date,
                customerReceipt,
                merchantReceipt,
                rawData,
                data
            )
            override fun toString() = "Error.ZVT(" +
                    "date=$date, " +
                    "customerReceipt=$customerReceipt, " +
                    "merchantReceipt=$merchantReceipt, " +
                    "rawData=$rawData, " +
                    "data=$data" +
                    ")"
        }

        class OPI(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: TransactionData?,
        ) : Error() {
            override fun compareTo(other: TerminalOperationStatus): Int = if (other is OPI) {
                date.compareTo(other.date) +
                        customerReceipt.compareTo(other.customerReceipt) +
                        merchantReceipt.compareTo(other.merchantReceipt) +
                        rawData.compareTo(other.rawData) +
                        if (data != null && other.data != null) {
                            data.compareTo(other.data)
                        } else if (data == null && other.data == null) {
                            0
                        } else {
                            -1
                        }
            } else {
                -1
            }

            override fun equals(other: Any?) = other is OPI &&
                    date == other.date &&
                    customerReceipt == other.customerReceipt &&
                    merchantReceipt == other.merchantReceipt &&
                    rawData == other.rawData &&
                    data == other.data

            override fun hashCode() = Objects.hash(
                date,
                customerReceipt,
                merchantReceipt,
                rawData,
                data
            )
            override fun toString() = "Error.OPI(" +
                    "date=$date, " +
                    "customerReceipt=$customerReceipt, " +
                    "merchantReceipt=$merchantReceipt, " +
                    "rawData=$rawData, " +
                    "data=$data" +
                    ")"
        }
    }

    override fun compareTo(other: TerminalOperationStatus): Int =
        if (other == this) {
            0
        } else {
            -1
        }
}
