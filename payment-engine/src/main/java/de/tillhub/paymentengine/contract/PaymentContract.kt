package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler
import java.math.BigDecimal
import java.util.Objects

class PaymentResultContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<PaymentRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: PaymentRequest): Intent {
        return input.config.contract.paymentIntent(context, input).also {
            analytics?.logOperation(AnalyticsMessageFactory.createPaymentOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(
            resultCode,
            intent,
            TerminalOperationStatus.Payment::class
        ).also {
            analytics?.logCommunication(
                protocol = intent?.getStringExtra(ExtraKeys.EXTRAS_PROTOCOL).orEmpty(),
                message = if (resultCode == Activity.RESULT_OK) {
                    AnalyticsMessageFactory.createResultOk(intent?.extras)
                } else {
                    AnalyticsMessageFactory.createResultCanceled(intent?.extras)
                }
            )
        }
}

/***
 * @param config - terminal config used for this request
 * @param transactionId - id of the transaction that will be created by the request (S-POS only)
 * @param amount - amount being charged in the created transaction
 * @param tip - tip being charged in the created transaction, by default it will be set to 0.00
 * @param currency - currency the transaction will be made in
 */
class PaymentRequest(
    val config: Terminal,
    val transactionId: String,
    val amount: BigDecimal,
    val tip: BigDecimal = BigDecimal.ZERO,
    val currency: ISOAlphaCurrency
) {
    override fun toString() = "PaymentRequest(" +
            "config=$config, " +
            "transactionId=$transactionId, " +
            "amount=$amount, " +
            "tip=$tip, " +
            "currency=$currency" +
            ")"
    override fun equals(other: Any?) = other is PaymentRequest &&
            config == other.config &&
            transactionId == other.transactionId &&
            amount == other.amount &&
            tip == other.tip &&
            currency == other.currency
    override fun hashCode() = Objects.hash(config, transactionId, amount, tip, currency)
}