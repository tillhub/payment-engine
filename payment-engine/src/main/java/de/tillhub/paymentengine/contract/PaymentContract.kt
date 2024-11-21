package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIPaymentActivity
import de.tillhub.paymentengine.spos.AnalyticsMessageFactory
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.zvt.ui.CardPaymentActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentResultContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<PaymentRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: PaymentRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount + input.tip)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }
            is Terminal.OPI -> Intent(context, OPIPaymentActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount + input.tip)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }
            is Terminal.SPOS -> SPOSIntentFactory.createPaymentIntent(input)
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createPaymentOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return if (resultCode == Activity.RESULT_OK) {
            if (intent?.extras?.containsKey(ExtraKeys.EXTRAS_RESULT) == true) {
                intent.extras?.let {
                    BundleCompat.getParcelable(
                        it,
                        ExtraKeys.EXTRAS_RESULT,
                        TerminalOperationStatus::class.java
                    )
                } ?: TerminalOperationStatus.Canceled
            } else {
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent).also {
                    analytics?.logCommunication(
                        protocol = SPOS_PROTOCOL,
                        message = AnalyticsMessageFactory.createResultOk(intent?.extras)
                    )
                }
            }
        } else {
            if (intent?.extras?.containsKey(SPOSKey.ResultExtra.ERROR) == true) {
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent).also {
                    analytics?.logCommunication(
                        protocol = SPOS_PROTOCOL,
                        message = AnalyticsMessageFactory.createResultCanceled(intent.extras)
                    )
                }
            } else {
                TerminalOperationStatus.Canceled
            }
        }
    }

    companion object {
        private const val SPOS_PROTOCOL = "SPOS"
    }
}

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