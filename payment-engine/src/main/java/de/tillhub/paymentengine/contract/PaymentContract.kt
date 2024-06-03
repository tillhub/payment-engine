package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIPaymentActivity
import de.tillhub.paymentengine.zvt.ui.CardPaymentActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentResultContract : ActivityResultContract<PaymentRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: PaymentRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }
            is Terminal.OPI -> Intent(context, OPIPaymentActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
        } ?: TerminalOperationStatus.Canceled
    }
}

class PaymentRequest(
    val config: Terminal,
    val amount: BigDecimal,
    val currency: ISOAlphaCurrency
) {
    override fun toString() = "PaymentRequest(config=$config, amount=$amount, currency=$currency)"
    override fun equals(other: Any?) = other is PaymentRequest &&
            config == other.config &&
            amount == other.amount &&
            currency == other.currency
    override fun hashCode() = Objects.hash(config, amount, currency)
}