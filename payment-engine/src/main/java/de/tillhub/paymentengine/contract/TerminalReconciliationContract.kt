package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIReconciliationActivity
import de.tillhub.paymentengine.zvt.ui.TerminalReconciliationActivity

class TerminalReconciliationContract : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is Terminal.ZVT -> Intent(context, TerminalReconciliationActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }

            is Terminal.OPI -> Intent(context, OPIReconciliationActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
        } ?: TerminalOperationStatus.Canceled
    }
}