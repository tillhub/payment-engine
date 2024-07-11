package de.tillhub.paymentengine.opi.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.OPIChannelController
import de.tillhub.paymentengine.opi.OPIChannelControllerImpl
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.Currency

internal class OPITerminalViewModel(
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
                    is OPIOperationStatus.Result.Error -> State.ResultError(
                        data = status.toTerminalOperation()
                    )
                    is OPIOperationStatus.Result.Success -> State.ResultSuccess(
                        data = status.toTerminalOperation()
                    )
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
            val modifiedAmount = modifyAmountForOpi(amount, currency)
            opiChannelController.initiateCardPayment(modifiedAmount, currency)
        }
    }

    fun startPaymentReversal(stan: String) {
        viewModelScope.launch {
            opiChannelController.initiatePaymentReversal(stan)
        }
    }

    fun startPartialRefund(amount: BigDecimal, currency: ISOAlphaCurrency) {
        viewModelScope.launch {
            val modifiedAmount = modifyAmountForOpi(amount, currency)
            opiChannelController.initiatePartialRefund(modifiedAmount, currency)
        }
    }

    fun startReconciliation() {
        viewModelScope.launch {
            opiChannelController.initiateReconciliation()
        }
    }

    fun setBringToFront(bringToFront: () -> Unit) {
        opiChannelController.setBringToFront(bringToFront)
    }

    @VisibleForTesting
    fun modifyAmountForOpi(amount: BigDecimal, currency: ISOAlphaCurrency): BigDecimal =
        amount.scaleByPowerOfTen(
            Currency.getInstance(currency.value).defaultFractionDigits.unaryMinus()
        )

    internal sealed class State {
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
            val data: TerminalOperationStatus.Error,
        ) : State()

        data class ResultSuccess(
            val data: TerminalOperationStatus.Success
        ) : State()
    }
}