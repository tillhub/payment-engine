package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler

class TerminalConnectContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        analytics?.logOperation("Operation: TERMINAL_CONNECT\n$input")
        return if (input is Terminal.SPOS) {
            SPOSIntentFactory.createConnectIntent(input)
        } else {
            throw UnsupportedOperationException("Connect only supported for S-POS terminals")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return SPOSResponseHandler.handleTerminalConnectResponse(resultCode, intent, analytics)
    }
}