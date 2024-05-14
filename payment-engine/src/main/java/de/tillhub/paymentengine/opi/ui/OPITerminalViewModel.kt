package de.tillhub.paymentengine.opi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.opi.OPIChannelController
import de.tillhub.paymentengine.opi.OPIChannelControllerImpl
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.math.BigDecimal

class OPITerminalViewModel(
    private val opiChannelController: OPIChannelController = OPIChannelControllerImpl()
) : ViewModel() {

    private val _opiOperationState: MutableLiveData<State> =
        MutableLiveData(State.Idle)
    val opiOperationState: LiveData<State> = _opiOperationState

    fun init(terminal: Terminal.OPI) {
        opiChannelController.init(terminal)

        viewModelScope.launch {
            opiChannelController.operationState.collect { status ->
                _opiOperationState.value = when (status) {
                    OPIOperationStatus.Idle -> State.Idle
                    is OPIOperationStatus.Pending.Operation -> {
                        if (status.messageLines.isEmpty()) {
                            State.Pending.NoMessage
                        } else {
                            val message = StringBuilder().apply {
                                status.messageLines.forEach { appendLine(it) }
                            }.toString()
                            State.Pending.WithMessage(message)
                        }
                    }
                    is OPIOperationStatus.Pending.Login -> State.Pending.Login
                    OPIOperationStatus.LoggedIn -> State.LoggedIn
                    is OPIOperationStatus.Error -> State.OperationError(
                        status,
                        status.message
                    )
                    is OPIOperationStatus.Result.Error -> State.ResultError(status)
                    is OPIOperationStatus.Result.Success -> State.ResultSuccess(status)
                }
            }
        }
    }

    fun onDestroy() {
        opiChannelController.close()
    }

    fun loginToTerminal() {
        viewModelScope.launch {
            opiChannelController.login()
        }
    }

    fun startPayment(amount: BigDecimal, currency: ISOAlphaCurrency) {
        viewModelScope.launch {
            opiChannelController.initiatePaymentReversal(amount, currency)
        }
    }

    fun startPaymentReversal(stan: String) {
        viewModelScope.launch {
            opiChannelController.initiatePaymentReversal(stan)
        }
    }

    sealed class State {
        data object Idle : State()

        data object LoggedIn : State()

        sealed class Pending : State() {
            data object NoMessage : Pending()
            data class WithMessage(val message: String) : Pending()
            data object Login : Pending()
        }

        data class OperationError(
            val data: OPIOperationStatus.Error,
            val message: String
        ) : State()

        data class ResultError(
            val data: OPIOperationStatus.Result.Error,
        ) : State()

        data class ResultSuccess(
            val data: OPIOperationStatus.Result.Success
        ) : State()
    }
}