package de.tillhub.paymentengine.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {

    private lateinit var paymentManager: CardPaymentManager
    lateinit var state: StateFlow<TerminalOperationStatus>

    fun init(paymentManager: CardPaymentManager) {
        this.paymentManager = paymentManager
        paymentManager.putTerminalConfig(Terminal.ZVT(
            name = "zvt",
            ipAddress = "192.168.178.109",
            port = 20007
        ))
        paymentManager.putTerminalConfig(Terminal.ZVT(
            name = "zvt-local",
            ipAddress = "127.0.0.1",
            port = 40007
        ))
        state = paymentManager.observePaymentState()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                TerminalOperationStatus.Waiting
            )
    }

    fun startPayment() {
        paymentManager.startPaymentTransaction(500.toBigDecimal(), ISOAlphaCurrency("EUR"), "zvt-local")
    }

    fun startRefund() {
        paymentManager.startRefundTransaction(600.toBigDecimal(), ISOAlphaCurrency("EUR"), "zvt-local")
    }

    fun startReversal() {
        paymentManager.startReversalTransaction("244", "zvt-local")
    }

    fun startReconciliation() {
        paymentManager.startReconciliation("zvt-local")
    }
}