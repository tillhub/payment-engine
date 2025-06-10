package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Objects

/**
 * Represents an error that occurred during a terminal operation.
 *
 * This class implements [Parcelable] to allow it to be passed between Android components (e.g., Activities).
 *
 * @property date The timestamp when the error occurred.
 * @property customerReceipt The customer receipt text, if available.
 * @property merchantReceipt The merchant receipt text, if available.
 * @property rawData Raw data associated with the error, often from the terminal.
 * @property data Parsed transaction data related to the error, if available.
 * @property resultCode A [TransactionResultCode] indicating the specific type of error.
 * @property isRecoverable A boolean flag indicating whether the tried transaction might be recoverable
 *                         (e.g., by retrying the operation).
 */
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