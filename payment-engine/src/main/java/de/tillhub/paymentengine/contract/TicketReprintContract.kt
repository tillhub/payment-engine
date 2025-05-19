package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.AnalyticsMessageFactory
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ResponseHandler

class TicketReprintContract(
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().paymentAnalytics
) : ActivityResultContract<Terminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: Terminal): Intent {
        return if (input is Terminal.External) {
            input.ticketReprintIntent(context, input)
        } else {
            throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
        }.also {
            analytics?.logOperation(AnalyticsMessageFactory.createTicketReprintOperation(input))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        ResponseHandler.parseResult(
            resultCode,
            intent,
            TerminalOperationStatus.TicketReprint::class
        ).also {
            analytics?.logCommunication(
                protocol = intent?.getStringExtra(ExtraKeys.EXTRAS_PROTOCOL).orEmpty(),
                message = AnalyticsMessageFactory.createResultOk(intent?.extras)
            )
        }
}