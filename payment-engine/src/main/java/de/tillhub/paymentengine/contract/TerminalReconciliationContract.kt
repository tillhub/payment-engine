package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler
import de.tillhub.paymentengine.opi.ui.OPIReconciliationActivity
import de.tillhub.paymentengine.spos.AnalyticsMessageFactory
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.zvt.ui.TerminalReconciliationActivity

class TerminalReconciliationContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is Terminal.ZVT -> Intent(context, TerminalReconciliationActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }

            is Terminal.OPI -> Intent(context, OPIReconciliationActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }

            is Terminal.SPOS -> SPOSIntentFactory.createReconciliationIntent()

            is Terminal.External -> Intent(context, input.reconciliationActivity).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createReconciliationOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(
            resultCode,
            intent,
            analytics,
            TerminalOperationStatus.Reconciliation::class
        )
}