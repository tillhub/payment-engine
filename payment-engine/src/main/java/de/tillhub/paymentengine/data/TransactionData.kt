package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class TransactionData(
    val terminalId: String,
    val transactionId: String,
    val cardCircuit: String,
    val cardPan: String,
    val paymentProvider: String,
) : Parcelable {

    override fun equals(other: Any?) = other is TransactionData &&
            terminalId == other.terminalId &&
            transactionId == other.transactionId &&
            cardCircuit == other.cardCircuit &&
            cardPan == other.cardPan &&
            paymentProvider == other.paymentProvider

    override fun hashCode() = Objects.hash(
        terminalId,
        transactionId,
        cardCircuit,
        cardPan,
        paymentProvider
    )
    override fun toString() = "TransactionData(" +
            "terminalId=$terminalId, " +
            "transactionId=$transactionId, " +
            "cardCircuit=$cardCircuit, " +
            "cardPan=$cardPan, " +
            "paymentProvider=$paymentProvider" +
            ")"
}