package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler
import de.tillhub.paymentengine.spos.AnalyticsMessageFactory
import de.tillhub.paymentengine.spos.SPOSIntentFactory

class TicketReprintContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return if (input is Terminal.SPOS) {
            SPOSIntentFactory.createTicketReprintIntent()
        } else {
            throw UnsupportedOperationException("Ticket reprint only supported for S-POS terminals")
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createTicketReprintOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(resultCode, intent, analytics)
}