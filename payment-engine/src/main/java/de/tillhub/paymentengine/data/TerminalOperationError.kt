package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Objects

@Suppress("LongParameterList")
@Parcelize
class TerminalOperationError(
    val date: Instant,
    val customerReceipt: String = "",
    val merchantReceipt: String = "",
    val rawData: String = "",
    val data: TransactionData? = null,
    val resultCode: TransactionResultCode,
    val isRecoverable: Boolean = false,
) : Parcelable {

    override fun equals(other: Any?) = other is TerminalOperationError &&
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