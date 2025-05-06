package de.tillhub.paymentengine.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.CardManager
import de.tillhub.paymentengine.ConnectionManager
import de.tillhub.paymentengine.PaymentManager
import de.tillhub.paymentengine.ReconciliationManager
import de.tillhub.paymentengine.RecoveryManager
import de.tillhub.paymentengine.RefundManager
import de.tillhub.paymentengine.ReversalManager
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import de.tillhub.paymentengine.softpay.data.SoftpayConfig
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
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
    private lateinit var recoveryManager: RecoveryManager

    val cardManagerState: StateFlow<TerminalOperationStatus> by lazy {
        merge(
            paymentManager.observePaymentState(),
            refundManager.observePaymentState(),
            reversalManager.observePaymentState(),
            reconciliationManager.observePaymentState(),
            connectionManager.observePaymentState(),
            recoveryManager.observePaymentState()
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

    fun initRecoveryManager(newRecoveryManager: RecoveryManager) {
        this.recoveryManager = newRecoveryManager.apply {
            setupTerminalConfigs(this)
        }
    }

    private fun setupTerminalConfigs(cardManager: CardManager) {
        cardManager.putTerminalConfig(
            Terminal.ZVT(
                id = "zvt-remote",
                ipAddress = REMOTE_IP,
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
                ipAddress = REMOTE_IP,
                port = 20002,
                port2 = 20007
            )
        )
        cardManager.putTerminalConfig(
            SPOSTerminal(
                id = "s-pos",
            )
        )
        cardManager.putTerminalConfig(
            SoftpayTerminal(
                id = "softpay",
                config = SoftpayConfig(
                    integratorId = BuildConfig.INTEGRATOR_ID,
                    accessId = BuildConfig.ACCESS_ID,
                    accessSecret = BuildConfig.ACCESS_SECRET,
                    merchantUsername = "RKDGDLKN",
                    merchantPassword = "FYWEAXC6YALF",
                )
            )
        )
    }

    fun startPayment() {
        paymentManager.startPaymentTransaction(
            UUID.randomUUID().toString(),
            500.toBigDecimal(),
            100.toBigDecimal(),
            ISOAlphaCurrency("EUR"),
            CONFIG_IN_USE
        )
    }

    fun startRefund() {
        refundManager.startRefundTransaction(
            transactionId = UUID.randomUUID().toString(),
            amount = 600.toBigDecimal(),
            currency = ISOAlphaCurrency("EUR"),
            configId = CONFIG_IN_USE
        )
    }

    fun startReversal() {
        reversalManager.startReversalTransaction(
            transactionId = UUID.randomUUID().toString(),
            amount = 500.toBigDecimal(),
            tip = 100.toBigDecimal(),
            currency = ISOAlphaCurrency("EUR"),
            receiptNo = "374",
            configId = CONFIG_IN_USE
        )
    }

    fun startReconciliation() {
        reconciliationManager.startReconciliation(CONFIG_IN_USE)
    }

    fun startConnect() {
        connectionManager.startConnect(CONFIG_IN_USE)
    }

    fun startSPOSDisconnect() {
        connectionManager.startSPOSDisconnect(CONFIG_IN_USE)
    }

    fun startRecovery() {
        recoveryManager.startRecovery(CONFIG_IN_USE)
    }

    companion object {
        private const val CONFIG_IN_USE = "softpay"
        private const val REMOTE_IP = "192.168.100.39"
    }
}