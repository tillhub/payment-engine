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
import java.util.UUID

class MainViewModel : ViewModel() {

    private lateinit var paymentManager: PaymentManager
    private lateinit var refundManager: RefundManager
    private lateinit var reversalManager: ReversalManager
    private lateinit var reconciliationManager: ReconciliationManager
    private lateinit var connectionManager: ConnectionManager
    var terminalID: String = "s-pos"
    var remoteIP: String = ""

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
        cardManager.putTerminalConfig(
            Terminal.ZVT(
                id = "zvt-remote",
                ipAddress = remoteIP,
                port = 20007
            )
        )
        cardManager.putTerminalConfig(
            Terminal.ZVT(
                id = "zvt-local",
                ipAddress = "127.0.0.1",
                port = 40007
            )
        )
        cardManager.putTerminalConfig(
            Terminal.OPI(
                id = "opi",
                ipAddress = remoteIP,
                port = 20002,
                port2 = 20007
            )
        )
        cardManager.putTerminalConfig(
            Terminal.SPOS(
                id = "s-pos",
            )
        )
    }

    fun startPayment() {
        initPaymentManager(this.paymentManager)
        paymentManager.startPaymentTransaction(
            UUID.randomUUID().toString(),
            500.toBigDecimal(),
            100.toBigDecimal(),
            ISOAlphaCurrency("EUR"),
            terminalID
        )
    }

    fun startRefund() {
        initRefundManager(this.refundManager)
        refundManager.startRefundTransaction(
            transactionId = UUID.randomUUID().toString(),
            amount = 600.toBigDecimal(),
            currency = ISOAlphaCurrency("EUR"),
            configId = terminalID
        )
    }

    fun startReversal() {
        initReversalManager(this.reversalManager)
        reversalManager.startReversalTransaction(
            transactionId = UUID.randomUUID().toString(),
            amount = 500.toBigDecimal(),
            tip = 100.toBigDecimal(),
            currency = ISOAlphaCurrency("EUR"),
            receiptNo = "374",
            configId = terminalID
        )
    }

    fun startReconciliation() {
        initReconciliationManager(this.reconciliationManager)
        reconciliationManager.startReconciliation(terminalID)
    }

    fun startSPOSConnect() {
        initConnectionManager(this.connectionManager)
        connectionManager.startConnect(terminalID)
    }

    fun startSPOSDisconnect() {
        connectionManager.startSPOSDisconnect(terminalID)
    }

}