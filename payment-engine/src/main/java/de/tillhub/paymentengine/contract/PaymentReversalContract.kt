package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler
import de.tillhub.paymentengine.opi.ui.OPIPaymentReversalActivity
import de.tillhub.paymentengine.spos.AnalyticsMessageFactory
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.zvt.ui.CardPaymentReversalActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentReversalContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<ReversalRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: ReversalRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentReversalActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
            }

            is Terminal.OPI -> Intent(context, OPIPaymentReversalActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
            }

            is Terminal.SPOS -> SPOSIntentFactory.createPaymentReversalIntent(input)
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createReversalOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(resultCode, intent, analytics)
}

/***
 * @param config - terminal config used for this request
 * @param transactionId - id of the transaction that will be created by the request (S-POS only)
 * @param amount - amount of the original transaction being cancelled
 * @param tip - tip of the original transaction being cancelled, by default it will be set to 0.00
 * @param currency - currency the transaction will be made in
 * @param receiptNo - the receipt number of the original transaction being cancelled
 */
class ReversalRequest(
    val config: Terminal,
    val transactionId: String,
    val amount: BigDecimal,
    val tip: BigDecimal = BigDecimal.ZERO,
    val currency: ISOAlphaCurrency,
    val receiptNo: String
) {
    override fun toString() = "ReversalRequest(" +
            "config=$config, " +
            "transactionId=$transactionId, " +
            "amount=$amount, " +
            "tip=$tip, " +
            "currency=$currency" +
            "receiptNo=$receiptNo, " +
            ")"

    override fun equals(other: Any?) = other is ReversalRequest &&
            config == other.config &&
            transactionId == other.transactionId &&
            amount == other.amount &&
            tip == other.tip &&
            currency == other.currency &&
            receiptNo == other.receiptNo

    override fun hashCode() = Objects.hash(config, transactionId, amount, tip, currency, receiptNo)
}