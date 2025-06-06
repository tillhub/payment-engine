package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExternalTerminal
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler
import de.tillhub.paymentengine.opi.data.OPITerminal
import de.tillhub.paymentengine.opi.ui.OPILoginActivity
import de.tillhub.paymentengine.zvt.data.ZVTTerminal
import de.tillhub.paymentengine.zvt.ui.TerminalLoginActivity

class TerminalConnectContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is ZVTTerminal -> Intent(context, TerminalLoginActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
            is OPITerminal -> Intent(context, OPILoginActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input)
            }
            is ExternalTerminal -> input.connectIntent(context, input)

            else -> throw IllegalArgumentException("Unknown terminal type: $input")
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createConnectOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(
            resultCode,
            intent,
            TerminalOperationStatus.Login::class
        ).also {
            analytics?.logCommunication(
                protocol = intent?.getStringExtra(ExtraKeys.EXTRAS_PROTOCOL).orEmpty(),
                message = if (resultCode == Activity.RESULT_OK) {
                    AnalyticsMessageFactory.createResultOk(intent?.extras)
                } else {
                    AnalyticsMessageFactory.createResultCanceled(intent?.extras)
                }
            )
        }
}