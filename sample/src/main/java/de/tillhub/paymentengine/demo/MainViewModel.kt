package de.tillhub.paymentengine.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.tillhub.paymentengine.CardManager
import de.tillhub.paymentengine.ConnectionManager
import de.tillhub.paymentengine.PaymentManager
import de.tillhub.paymentengine.ReconciliationManager
import de.tillhub.paymentengine.RefundManager
import de.tillhub.paymentengine.ReversalManager
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.demo.di.SampleAppApplication
import de.tillhub.paymentengine.demo.storage.DeviceRepository
import de.tillhub.paymentengine.demo.storage.TerminalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class MainViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private lateinit var paymentManager: PaymentManager
    private lateinit var refundManager: RefundManager
    private lateinit var reversalManager: ReversalManager
    private lateinit var reconciliationManager: ReconciliationManager
    private lateinit var connectionManager: ConnectionManager
    var terminalID: MutableStateFlow<String> = MutableStateFlow("s-pos")

    val terminalData: StateFlow<TerminalData> = deviceRepository.terminal.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(),
        initialValue = TerminalData()
    )

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
                ipAddress = terminalData.value.ipAddress!!,
                port = if (terminalData.value.port1?.toIntOrNull() != null) terminalData.value.port1!!.toInt() else 20007
            )
        )
        cardManager.putTerminalConfig(
            Terminal.ZVT(
                id = "zvt-local",
                ipAddress = "127.0.0.1",
                port = if (terminalData.value.port1?.toIntOrNull() != null) terminalData.value.port1!!.toInt() else 40007
            )
        )
        cardManager.putTerminalConfig(
            Terminal.OPI(
                id = "opi",
                ipAddress = terminalData.value.ipAddress!!,
                port = if (terminalData.value.port1?.toIntOrNull() != null) terminalData.value.port1!!.toInt() else 20002,
                port2 = if (terminalData.value.port2?.toIntOrNull() != null) terminalData.value.port2!!.toInt() else 20007
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
            terminalID.value
        )
    }

    fun startRefund() {
        initRefundManager(this.refundManager)
        refundManager.startRefundTransaction(
            transactionId = UUID.randomUUID().toString(),
            amount = 600.toBigDecimal(),
            currency = ISOAlphaCurrency("EUR"),
            configId = terminalID.value
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
            configId = terminalID.value
        )
    }

    fun startReconciliation() {
        initReconciliationManager(this.reconciliationManager)
        reconciliationManager.startReconciliation(terminalID.value)
    }

    fun startConnect() {
        initConnectionManager(this.connectionManager)
        connectionManager.startConnect(terminalID.value)
    }

    fun startSPOSDisconnect() {
        connectionManager.startSPOSDisconnect(terminalID.value)
    }

    fun updateIpAddress(newValue: String) {
        deviceRepository.updateTerminal(newValue, terminalData.value.port1.orEmpty(), terminalData.value.port2.orEmpty())
    }
    fun updatePort1(newValue: String) {
        deviceRepository.updateTerminal(terminalData.value.ipAddress.orEmpty(), newValue, terminalData.value.port2.orEmpty())
    }
    fun updatePort2(newValue: String) {
        deviceRepository.updateTerminal(terminalData.value.ipAddress.orEmpty(), terminalData.value.port1.orEmpty(), newValue)
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SampleAppApplication)
                MainViewModel(
                    application.deviceRepository
                )
            }
        }
    }
}