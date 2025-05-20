package de.tillhub.paymentengine.contract

import android.app.Activity
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
import de.tillhub.paymentengine.opi.ui.OPIPartialRefundActivity
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.zvt.ui.CardPaymentPartialRefundActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentRefundContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<RefundRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: RefundRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentPartialRefundActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }

            is Terminal.OPI -> Intent(context, OPIPartialRefundActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }

            is Terminal.External -> input.config.refundIntent(context, input)
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createRefundOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(
            resultCode,
            intent,
            TerminalOperationStatus.Refund::class
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
 * @param amount - amount being refunded in this transaction
 * @param currency - currency the transaction will be made in
 */
class RefundRequest(
    val config: Terminal,
    val transactionId: String,
    val amount: BigDecimal,
    val currency: ISOAlphaCurrency
) {
    override fun toString() = "RefundRequest(config=$config, amount=$amount, currency=$currency)"
    override fun equals(other: Any?) = other is RefundRequest &&
            config == other.config &&
            amount == other.amount &&
            currency == other.currency
    override fun hashCode() = Objects.hash(config, amount, currency)
}