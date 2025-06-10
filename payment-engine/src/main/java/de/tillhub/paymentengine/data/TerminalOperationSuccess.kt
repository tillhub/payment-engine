package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Objects

/**
 * Represents a successful terminal operation.
 *
 * This class encapsulates the details of a successful transaction or operation performed on a payment terminal.
 * It includes information such as the date of the operation, receipts for both the customer and merchant,
 * the raw data received from the terminal, parsed transaction data (if available), and a flag indicating
 * if a reprint of the receipt is required.
 *
 * This class implements [Parcelable] to allow it to be passed between Android components (e.g., Activities).
 *
 * @property date The [Instant] the operation was successfully completed.
 * @property customerReceipt A string representation of the customer's receipt.
 * @property merchantReceipt A string representation of the merchant's receipt.
 * @property rawData The raw string data received from the terminal upon successful completion.
 * @property data Optional [TransactionData] parsed from the `rawData`.
 *                This may be null if parsing fails or is not applicable.
 * @property reprintRequired A boolean flag indicating whether a reprint of the receipt is necessary.
 *                           Defaults to `false`.
 */
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