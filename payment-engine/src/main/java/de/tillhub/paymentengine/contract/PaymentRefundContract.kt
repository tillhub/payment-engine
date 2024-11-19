package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIPartialRefundActivity
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.zvt.ui.CardPaymentPartialRefundActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentRefundContract : ActivityResultContract<RefundRequest, TerminalOperationStatus>() {

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

            is Terminal.SPOS -> SPOSIntentFactory.createPaymentRefundIntent(input)
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
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent)
            }
        } else {
            if (intent?.extras?.containsKey(SPOSKey.ResultExtra.ERROR) == true) {
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent)
            } else {
                TerminalOperationStatus.Canceled
            }
        }
    }
}

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