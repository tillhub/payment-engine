package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.helper.ResponseHandler

class TerminalDisconnectContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return if (input is Terminal.External) {
            input.disconnectIntent(context, input)
        } else {
            throw UnsupportedOperationException("Disconnect is not supported by this terminal")
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createDisconnectOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return ResponseHandler.parseResult(
            resultCode,
            intent,
            TerminalOperationStatus.Login::class
        )
    }
}