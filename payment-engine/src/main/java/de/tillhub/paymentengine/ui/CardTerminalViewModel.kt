package de.tillhub.paymentengine.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.data.LavegoReceiptBuilder
import de.tillhub.paymentengine.data.LavegoTransactionData
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.data.getOrNull
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.TerminalConfigImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

class CardTerminalViewModel(
    private val terminalConfig: TerminalConfig = TerminalConfigImpl(),
    private val lavegoTransactionDataConverter: LavegoTransactionDataConverter = LavegoTransactionDataConverter()
) : ViewModel() {

    companion object {
        private const val SERVICE_START_DELAY: Long = 500
    }

    private var lastReceipt: LavegoReceiptBuilder? = null
    private var lastData: String? = null

    private val _terminalOperationState: MutableLiveData<State> =
        MutableLiveData(State.Idle)
    val terminalOperationState: LiveData<State> = _terminalOperationState

    fun init() {
        viewModelScope.launch {
            if (_terminalOperationState.value == State.Idle) {
                delay(SERVICE_START_DELAY)
                _terminalOperationState.value = State.Setup
            }
        }
    }

    fun onStatus(status: String) {
        lastData = status
    }

    fun onReceipt(receipt: String) {
        if (lastReceipt == null) {
            lastReceipt = LavegoReceiptBuilder()
        }
        if (receipt.contains('\n')) {
            lastReceipt!!.addBlock(receipt)
        } else {
            lastReceipt!!.addLine(receipt)
        }
    }

    fun onCompletion() {
        viewModelScope.launch {
            _terminalOperationState.value = when (_terminalOperationState.value) {
                State.Setup -> State.Operation
                State.Operation -> State.Success(
                    date = terminalConfig.timeNow(),
                    customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                    merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                    rawData = lastData.orEmpty(),
                    data = lastData?.let {
                        lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
                    }
                )
                else -> throw IllegalStateException("Illegal state: ${terminalOperationState.value}")
            }
        }
    }

    fun onError() {
        viewModelScope.launch {
            val data = lastData?.let {
                lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
            }
            _terminalOperationState.value = State.Error(
                date = terminalConfig.timeNow(),
                customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                rawData = lastData.orEmpty(),
                data = data,
                resultCode = ResultCodeSets.get(data?.resultCode)
            )
        }
    }

    sealed class State {
        data object Idle : State()
        data object Setup : State()
        data object Operation : State()
        data class Error(
            val date: Instant,
            val customerReceipt: String,
            val merchantReceipt: String,
            val rawData: String,
            val data: LavegoTransactionData?,
            val resultCode: TransactionResultCode
        ) : State() {
            fun toTerminalOperation() =
                TerminalOperationStatus.Error.ZVT(
                    date, customerReceipt, merchantReceipt, rawData, data, resultCode
                )
        }
        data class Success(
            val date: Instant,
            val customerReceipt: String,
            val merchantReceipt: String,
            val rawData: String,
            val data: LavegoTransactionData?
        ) : State() {
            fun toTerminalOperation() =
                TerminalOperationStatus.Success.ZVT(
                    date, customerReceipt, merchantReceipt, rawData, data
                )
        }
    }
}
