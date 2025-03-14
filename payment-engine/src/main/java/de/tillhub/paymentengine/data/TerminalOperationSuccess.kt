package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Objects

@Parcelize
class TerminalOperationSuccess(
    val date: Instant,
    val customerReceipt: String,
    val merchantReceipt: String,
    val rawData: String,
    val data: TransactionData?,
    val reprintRequired: Boolean = false
) : Parcelable {

    override fun equals(other: Any?) = other is TerminalOperationSuccess &&
            date == other.date &&
            customerReceipt == other.customerReceipt &&
            merchantReceipt == other.merchantReceipt &&
            rawData == other.rawData &&
            data == other.data &&
            reprintRequired == other.reprintRequired

    override fun hashCode() = Objects.hash(
        date,
        customerReceipt,
        merchantReceipt,
        rawData,
        data,
        reprintRequired
    )

    override fun toString() = "Success(" +
            "date=$date, " +
            "customerReceipt=$customerReceipt, " +
            "merchantReceipt=$merchantReceipt, " +
            "rawData=$rawData, " +
            "data=$data," +
            "reprintRequired=$reprintRequired" +
            ")"
}