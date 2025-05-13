package de.tillhub.paymentengine.softpay.ui.connect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
import de.tillhub.paymentengine.softpay.helpers.collectWithOwner
import de.tillhub.paymentengine.softpay.ui.SoftpayTerminalActivity
import java.time.Instant

internal class SoftpayConnectActivity : SoftpayTerminalActivity() {

    private val viewModel by viewModels<SoftpayConnectViewModel> { SoftpayConnectViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.collectWithOwner(this) { state ->
            when (state) {
                ConnectState.Idle -> Unit
                is ConnectState.Error -> handleError(state)
                is ConnectState.Success -> handleSuccess(state)
            }
        }
    }

    override fun startOperation() {
        viewModel.readTerminal()
    }

    override fun handleError(date: Instant, resultCode: TransactionResultCode) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Login.Error(
                        date = date,
                        rawData = "",
                        resultCode = resultCode
                    )
                )
            }
        )
        finish()
    }

    private fun handleError(error: ConnectState.Error) =
        handleError(error.date, error.resultCode)

    private fun handleSuccess(success: ConnectState.Success) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Login.Connected(
                        date = success.date,
                        rawData = "",
                        terminalType = SoftpayTerminal.TYPE,
                        terminalId = success.terminalId
                    )
                )
            }
        )
        finish()
    }
}
