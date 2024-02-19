package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.ui.TerminalReconciliationActivity

class TerminalReconciliationContract : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is Terminal.OPI -> TODO("Not supported yet")
            is Terminal.ZVT -> Intent(context, TerminalReconciliationActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
        } ?: TerminalOperationStatus.Canceled
    }

    companion object {
        const val REGISTER_KEY = "reconciliation_register_key"
    }
}