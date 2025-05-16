package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPILoginActivity
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.zvt.ui.TerminalLoginActivity

class TerminalConnectContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is Terminal.OPI -> Intent(context, OPILoginActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
            is Terminal.ZVT -> Intent(context, TerminalLoginActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }

            is Terminal.External -> input.connectIntent(context, input)
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createConnectOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.extras?.let {
                BundleCompat.getParcelable(
                    it,
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus::class.java
                )
            } ?: TerminalOperationStatus.Login.Canceled
        } else {
            TerminalOperationStatus.Login.Canceled
        }
    }
}