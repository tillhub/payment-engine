package de.tillhub.paymentengine.opi

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.opi.common.modifyAmountForOpi
import de.tillhub.paymentengine.opi.common.startAsForegroundService
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

internal class OPIService(
    private val opiController: OPIChannelController = OPIChannelControllerImpl()
) : LifecycleService() {

    private val binder = OPIServiceLocalBinder(this)

    private val _opiOperationState: MutableStateFlow<State> = MutableStateFlow(State.NotInitialized)
    val opiOperationState: StateFlow<State> = _opiOperationState

    private var bringToFront: (() -> Unit)? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        startAsForegroundService()
    }

    override fun onDestroy() {
        super.onDestroy()
        opiController.close()
        bringToFront = null
    }

    fun setBringToFront(bringToFront: () -> Unit) {
        this.bringToFront = bringToFront
    }

    fun init(terminal: Terminal.OPI) {
        opiController.init(terminal)

        lifecycleScope.launch {
            opiController.operationState.collect { status ->
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
                }.also {
                    if (status is OPIOperationStatus.Result) {
                        bringToFront?.invoke()
                    }
                }
            }
        }
    }

    fun loginToTerminal() {
        lifecycleScope.launch {
            opiController.login()
        }
    }

    fun startPayment(amount: BigDecimal, currency: ISOAlphaCurrency) {
        lifecycleScope.launch {
            val modifiedAmount = amount.modifyAmountForOpi(currency)
            opiController.initiateCardPayment(modifiedAmount, currency)
        }
    }

    fun startPaymentReversal(stan: String) {
        lifecycleScope.launch {
            opiController.initiatePaymentReversal(stan)
        }
    }

    fun startPartialRefund(amount: BigDecimal, currency: ISOAlphaCurrency) {
        lifecycleScope.launch {
            val modifiedAmount = amount.modifyAmountForOpi(currency)
            opiController.initiatePartialRefund(modifiedAmount, currency)
        }
    }

    fun startReconciliation() {
        lifecycleScope.launch {
            opiController.initiateReconciliation()
        }
    }

    fun startLogin() {
        lifecycleScope.launch {
            opiController.initiateLogin()
        }
    }

    fun abortRequest() {
        lifecycleScope.launch {
            opiController.abortRequest()
        }
    }

    class OPIServiceLocalBinder(
        private val instance: OPIService
    ) : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun service(): OPIService = instance
    }

    internal sealed class State {
        data object NotInitialized : State()
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
            val data: TerminalOperationError,
        ) : State()

        data class ResultSuccess(
            val data: TerminalOperationSuccess
        ) : State()
    }
}