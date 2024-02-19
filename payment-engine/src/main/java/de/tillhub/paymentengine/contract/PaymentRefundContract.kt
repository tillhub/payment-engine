package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.ui.CardPaymentPartialRefundActivity
import java.math.BigDecimal

class PaymentRefundContract : ActivityResultContract<RefundRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: RefundRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentPartialRefundActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
                putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            BundleCompat.getParcelable(it,
                ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
        } ?: TerminalOperationStatus.Canceled
    }

    companion object {
        const val REGISTER_KEY = "refund_register_key"
    }
}

data class RefundRequest(
    val config: Terminal,
    val amount: BigDecimal,
    val currency: ISOAlphaCurrency
)