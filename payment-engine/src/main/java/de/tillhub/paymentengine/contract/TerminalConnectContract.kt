package de.tillhub.paymentengine.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler

class TerminalConnectContract : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        return if (input is Terminal.SPOS) {
            SPOSIntentFactory.createConnectIntent(input)
        } else {
            throw UnsupportedOperationException("Connect only supported for S-POS terminals")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return SPOSResponseHandler.handleTerminalConnectResponse(resultCode, intent)
    }
}