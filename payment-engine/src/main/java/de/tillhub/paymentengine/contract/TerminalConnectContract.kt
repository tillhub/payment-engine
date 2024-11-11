package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import java.time.Instant

class TerminalConnectContract : ActivityResultContract<Terminal, TerminalOperationStatus>() {
    override fun createIntent(context: Context, input: Terminal): Intent {
        return when (input) {
            is Terminal.SPOS -> Intent(CONNECT_ACTION).apply {
                putExtra(EXTRA_APP_ID, input.appId)
            }
            else -> throw UnsupportedOperationException("Connect only supported for S-POS terminals")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return if (resultCode == Activity.RESULT_OK) {
            TerminalOperationStatus.Success.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = "",
                data = null
            )
        } else {
            TerminalOperationStatus.Canceled
        }
    }

    companion object {
        private const val CONNECT_ACTION = "de.spayment.akzeptanz.S_SWITCH_CONNECT"
        private const val EXTRA_APP_ID = "APP_ID"
    }
}