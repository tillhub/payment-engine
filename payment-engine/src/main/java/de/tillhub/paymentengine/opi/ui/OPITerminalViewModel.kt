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
import kotlinx.coroutines.flow.collect
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
                    is OPIOperationStatus.Pending -> {
                        if (status.messageLines.isEmpty()) {
                            State.Pending.NoMessage
                        } else {
                            val message = StringBuilder().apply {
                                status.messageLines.forEach { appendLine(it) }
                            }.toString()
                            State.Pending.WithMessage(message)
                        }
                    }
                    is OPIOperationStatus.Error -> State.Error(status)
                    is OPIOperationStatus.Success -> State.Success(status)
                }
            }
        }
    }

    fun onDestroy() {
        opiChannelController.close()
    }

    fun startPayment(amount: BigDecimal, currency: ISOAlphaCurrency) {
        viewModelScope.launch {
            opiChannelController.initiateCardPayment(amount, currency)
        }
    }

    sealed class State {
        data object Idle : State()
        sealed class Pending : State() {
            data object NoMessage : Pending()
            data class WithMessage(val message: String) : Pending()
        }

        data class Error(
            val data: OPIOperationStatus.Error,
        ) : State()

        data class Success(
            val data: OPIOperationStatus.Success
        ) : State()
    }
}