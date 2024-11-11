package de.tillhub.paymentengine.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.CardManager
import de.tillhub.paymentengine.ConnectionManager
import de.tillhub.paymentengine.PaymentManager
import de.tillhub.paymentengine.ReconciliationManager
import de.tillhub.paymentengine.RefundManager
import de.tillhub.paymentengine.ReversalManager
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {

    private lateinit var paymentManager: PaymentManager
    private lateinit var refundManager: RefundManager
    private lateinit var reversalManager: ReversalManager
    private lateinit var reconciliationManager: ReconciliationManager
    private lateinit var connectionManager: ConnectionManager

    val cardManagerState: StateFlow<TerminalOperationStatus> by lazy {
        merge(
            paymentManager.observePaymentState(),
            refundManager.observePaymentState(),
            reversalManager.observePaymentState(),
            reconciliationManager.observePaymentState(),
            connectionManager.observePaymentState()
        ).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                TerminalOperationStatus.Waiting
        )
    }

    fun initPaymentManager(paymentManager: PaymentManager) {
        this.paymentManager = paymentManager.apply {
            setupTerminalConfigs(this)
        }
    }

    fun initRefundManager(refundManager: RefundManager) {
        this.refundManager = refundManager.apply {
            setupTerminalConfigs(this)
        }
    }

    fun initReversalManager(reversalManager: ReversalManager) {
        this.reversalManager = reversalManager.apply {
            setupTerminalConfigs(this)
        }
    }

    fun initReconciliationManager(reconciliationManager: ReconciliationManager) {
        this.reconciliationManager = reconciliationManager.apply {
            setupTerminalConfigs(this)
        }
    }

    fun initConnectionManager(connectionManager: ConnectionManager) {
        this.connectionManager = connectionManager.apply {
            setupTerminalConfigs(this)
        }
    }

    private fun setupTerminalConfigs(cardManager: CardManager) {
        cardManager.putTerminalConfig(Terminal.ZVT(
            name = "zvt-remote",
            ipAddress = REMOTE_IP,
            port = 20007
        ))
        cardManager.putTerminalConfig(Terminal.ZVT(
            name = "zvt-local",
            ipAddress = "127.0.0.1",
            port = 40007
        ))
        cardManager.putTerminalConfig(Terminal.OPI(
            name = "opi",
            ipAddress = REMOTE_IP,
            port = 20002,
            port2 = 20007
        ))
        cardManager.putTerminalConfig(Terminal.SPOS(
            name = "s-pos",
        ))
    }

    fun startPayment() {
        paymentManager.startPaymentTransaction(
            500.toBigDecimal(),
            100.toBigDecimal(),
            ISOAlphaCurrency("EUR"),
            "zvt-local"
        )
    }

    fun startRefund() {
        refundManager.startRefundTransaction(600.toBigDecimal(), ISOAlphaCurrency("EUR"), "opi")
    }

    fun startReversal() {
        reversalManager.startReversalTransaction("374", "opi")
    }

    fun startReconciliation() {
        reconciliationManager.startReconciliation("opi")
    }

    fun startSPOSConnect() {
        connectionManager.startSPOSConnect("s-pos")
    }

    fun startSPOSDisconnect() {
        connectionManager.startSPOSDisconnect("s-pos")
    }

    companion object {
        private const val REMOTE_IP = "192.168.1.121"
    }
}