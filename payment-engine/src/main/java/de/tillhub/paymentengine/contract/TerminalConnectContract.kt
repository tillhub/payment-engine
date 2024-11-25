package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.AnalyticsMessageFactory
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler

class TerminalConnectContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        return if (input is Terminal.SPOS) {
            SPOSIntentFactory.createConnectIntent(input)
        } else {
            throw UnsupportedOperationException("Connect only supported for S-POS terminals")
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createConnectOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return SPOSResponseHandler.handleTerminalConnectResponse(resultCode, intent).also {
            if (resultCode == Activity.RESULT_OK) {
                analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.RESPONSE_RESULT_OK
                )
            } else {
                analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.createResultCanceled(intent?.extras)
                )
            }
        }
    }

    companion object {
        private const val SPOS_PROTOCOL = "SPOS"
    }
}