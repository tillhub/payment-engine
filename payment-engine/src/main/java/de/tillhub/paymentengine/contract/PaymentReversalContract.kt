package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIPaymentReversalActivity
import de.tillhub.paymentengine.zvt.ui.CardPaymentReversalActivity
import java.util.Objects

class PaymentReversalContract : ActivityResultContract<ReversalRequest, TerminalOperationStatus>() {

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

            is Terminal.SPOS -> TODO()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
        } ?: TerminalOperationStatus.Canceled
    }
}

class ReversalRequest(
    val config: Terminal,
    val receiptNo: String
) {
    override fun toString() = "ReversalRequest(config=$config, receiptNo=$receiptNo)"
    override fun equals(other: Any?) = other is ReversalRequest &&
            config == other.config &&
            receiptNo == other.receiptNo
    override fun hashCode() = Objects.hash(config, receiptNo)
}