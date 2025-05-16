package de.tillhub.paymentengine.spos.data

import android.content.Intent
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import java.math.BigDecimal

internal object SPOSRequestBuilder {
    fun buildPaymentRequest(intent: Intent): PaymentRequest = PaymentRequest(
        config = readConfig(intent),
        transactionId = readTransactionId(intent),
        amount = readAmount(intent),
        tip = readTip(intent),
        currency = readCurrency(intent),
    )

    fun buildRefundRequest(intent: Intent): RefundRequest = RefundRequest(
        config = readConfig(intent),
        transactionId = readTransactionId(intent),
        amount = readAmount(intent),
        currency = readCurrency(intent),
    )

    fun buildReversalRequest(intent: Intent): ReversalRequest = ReversalRequest(
        config = readConfig(intent),
        transactionId = readTransactionId(intent),
        amount = readAmount(intent),
        tip = readTip(intent),
        currency = readCurrency(intent),
        receiptNo = readReceiptNo(intent),
    )

    private fun readConfig(intent: Intent): SPOSTerminal = intent.extras?.let {
        BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, SPOSTerminal::class.java)
            ?: throw IllegalArgumentException("TerminalActivity: Argument config is missing")
    } ?: throw IllegalArgumentException("TerminalActivity: Extras is null")

    private fun readAmount(intent: Intent): BigDecimal = intent.extras?.let {
        BundleCompat.getSerializable(it, ExtraKeys.EXTRA_AMOUNT, BigDecimal::class.java)
            ?: throw IllegalArgumentException("TerminalActivity: Argument amount is missing")
    } ?: throw IllegalArgumentException("TerminalActivity: Extras is null")

    private fun readTip(intent: Intent): BigDecimal = intent.extras?.let {
        BundleCompat.getSerializable(it, SPOSExtraKeys.EXTRA_TIP, BigDecimal::class.java)
            ?: throw IllegalArgumentException("TerminalActivity: Argument tip is missing")
    } ?: throw IllegalArgumentException("TerminalActivity: Extras is null")

    private fun readCurrency(intent: Intent): ISOAlphaCurrency = intent.extras?.let {
        BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency::class.java)
            ?: throw IllegalArgumentException("TerminalActivity: Argument currency is missing")
    } ?: throw IllegalArgumentException("TerminalActivity: Extras is null")

    private fun readTransactionId(intent: Intent): String =
        intent.getStringExtra(SPOSExtraKeys.EXTRA_TX_ID)
            ?: throw IllegalArgumentException("TerminalActivity: Argument TxId is missing")

    private fun readReceiptNo(intent: Intent): String =
        intent.getStringExtra(ExtraKeys.EXTRA_RECEIPT_NO)
            ?: throw IllegalArgumentException("TerminalActivity: Argument ReceiptNo is missing")
}