package de.tillhub.paymentengine.softpay.ui.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.R
import de.tillhub.paymentengine.softpay.SoftpayApplication
import de.tillhub.paymentengine.softpay.helpers.TerminalConfig
import de.tillhub.paymentengine.softpay.helpers.TerminalConfigImpl
import io.softpay.sdk.config.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

internal class SoftpayConnectViewModel(
    private val configManager: ConfigManager,
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val state: StateFlow<ConnectState> = _state

    fun readTerminal() {
        _state.value = if (configManager.configured) {
            ConnectState.Success(
                date = terminalConfig.timeNow(),
                terminalId = configManager.terminal!!.id
            )
        } else {
            ConnectState.Error(
                date = terminalConfig.timeNow(),
                resultCode = TransactionResultCode.Known(
                    errorMessage = R.string.softpay_error_terminal_not_configured,
                    recoveryMessages = listOf(
                        R.string.softpay_recovery_terminal_not_configured,
                        R.string.softpay_recovery_contact_support,
                    )
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SoftpayApplication)
                SoftpayConnectViewModel(
                    configManager = application.softpay().configManager,
                )
            }
        }
    }
}

internal sealed class ConnectState {
    data object Idle : ConnectState()
    data class Error(
        val date: Instant,
        val resultCode: TransactionResultCode
    ) : ConnectState()
    data class Success(
        val date: Instant,
        val terminalId: String
    ) : ConnectState()
}