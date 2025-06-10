package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Represents the data associated with a payment transaction.
 *
 * This class encapsulates various details about a transaction,
 * such as the terminal used, transaction identifiers, card information,
 * and the payment provider involved.
 *
 * It implements [Parcelable] to allow instances of this class
 * to be passed between Android components (e.g., Activities, Fragments).
 *
 * @property terminalType The type of the terminal used for the transaction (e.g., ZVT, OPI, ...)
 * @property terminalId The unique identifier of the terminal.
 * @property transactionId The unique identifier for this specific transaction.
 * @property cardCircuit The card network or scheme (e.g., Visa, Mastercard).
 * @property cardPan The Primary Account Number of the card used, masked for security.
 * @property paymentProvider The entity that processed the payment.
 */
@Parcelize
class TransactionData(
    val terminalType: String,
    val terminalId: String,
    val transactionId: String,
    val cardCircuit: String,
    val cardPan: String,
    val paymentProvider: String,
) : Parcelable {

    override fun equals(other: Any?) = other is TransactionData &&
            terminalType == other.terminalType &&
            terminalId == other.terminalId &&
            transactionId == other.transactionId &&
            cardCircuit == other.cardCircuit &&
            cardPan == other.cardPan &&
            paymentProvider == other.paymentProvider

    override fun hashCode() = Objects.hash(
        terminalType,
        terminalId,
        transactionId,
        cardCircuit,
        cardPan,
        paymentProvider
    )
    override fun toString() = "TransactionData(" +
            "terminalType=$terminalType, " +
            "terminalId=$terminalId, " +
            "transactionId=$transactionId, " +
            "cardCircuit=$cardCircuit, " +
            "cardPan=$cardPan, " +
            "paymentProvider=$paymentProvider" +
            ")"
}