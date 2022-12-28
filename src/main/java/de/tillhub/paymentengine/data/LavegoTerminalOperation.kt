package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
sealed class LavegoTerminalOperation : Parcelable {

    abstract val date: Instant
    abstract val customerReceipt: String
    abstract val merchantReceipt: String
    abstract val rawData: String
    abstract val data: LavegoTransactionData?

    data class Failed(
        override val date: Instant,
        override val customerReceipt: String,
        override val merchantReceipt: String,
        override val rawData: String,
        override val data: LavegoTransactionData?,
    ) : LavegoTerminalOperation()

    data class Success(
        override val date: Instant,
        override val customerReceipt: String,
        override val merchantReceipt: String,
        override val rawData: String,
        override val data: LavegoTransactionData?,
    ) : LavegoTerminalOperation()
}
