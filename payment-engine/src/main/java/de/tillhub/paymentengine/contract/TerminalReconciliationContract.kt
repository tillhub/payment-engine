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
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSKey
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

            is Terminal.SPOS -> SPOSIntentFactory.createReconciliationIntent()
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