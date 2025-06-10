package de.tillhub.paymentengine.zvt.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.cmds.CompletionForRegister
import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.data.TransactionData
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.TerminalConfigImpl
import de.tillhub.paymentengine.zvt.data.LavegoReceiptBuilder
import de.tillhub.paymentengine.zvt.data.LavegoTransactionData
import de.tillhub.paymentengine.zvt.data.LavegoTransactionDataConverter
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.time.Instant

internal class CardTerminalViewModel(
    private val terminalConfig: TerminalConfig = TerminalConfigImpl(),
    private val lavegoTransactionDataConverter: LavegoTransactionDataConverter = LavegoTransactionDataConverter()
) : ViewModel() {

    companion object {
        private const val SERVICE_START_DELAY: Long = 500
        private const val TIMEOUT_DELAY: Long = 30_000
    }

    private var lastReceipt: LavegoReceiptBuilder? = null
    private var lastData: String? = null

    private var abortOperationTriggered: Boolean = false

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

    fun parseTransactionNumber(receiptNo: String): Result<Long> {
        return try {
            Result.success(receiptNo.toLong())
        } catch (e: NumberFormatException) {
            Result.failure<Long>(e).also {
                _terminalOperationState.value = State.Error(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = "",
                    data = null,
                    resultCode = TransactionResultCode.Known(R.string.common_result_code_tx_number_error)
                )
            }
        }
    }

    fun onCompletion(moveToFront: () -> Unit) {
        viewModelScope.launch {
            _terminalOperationState.value = if (abortOperationTriggered) {
                moveToFront()
                State.OperationAborted
            } else {
                when (_terminalOperationState.value) {
                    State.Setup -> State.Operation
                    State.Operation -> {
                        moveToFront()
                        State.Success(
                            date = terminalConfig.timeNow(),
                            customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                            merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                            rawData = lastData.orEmpty(),
                            data = lastData?.let {
                                lavegoTransactionDataConverter.convertFromTxJson(it).getOrNull()
                            }
                        )
                    }

                    else -> error("Illegal state: ${terminalOperationState.value}")
                }
            }
        }
    }

    fun onError(moveToFront: () -> Unit) {
        moveToFront()
        viewModelScope.launch {
            val data = lastData?.let {
                lavegoTransactionDataConverter.convertFromTxJson(it).getOrNull()
            }
            _terminalOperationState.value = State.Error(
                date = terminalConfig.timeNow(),
                customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                rawData = lastData.orEmpty(),
                data = data,
                resultCode = ResultCodeSets.getZVTCode(data?.resultCode)
            )
        }
    }

    fun abortOperation(triggerAbort: () -> Unit) {
        if (terminalOperationState.value is State.Operation) {
            abortOperationTriggered = true
            triggerAbort()

            viewModelScope.launch {
                delay(TIMEOUT_DELAY)
                _terminalOperationState.value = State.OperationAborted
            }
        } else if (terminalOperationState.value is State.Setup) {
            _terminalOperationState.value = State.OperationAborted
        }
    }

    fun setupFinished(completion: String, moveToFront: () -> Unit) {
        viewModelScope.launch {
            val apduString = lavegoTransactionDataConverter.convertFromLoginJson(completion)
            val apdu = Apdu(apduString.getOrNull()?.responseApdu)
            val status = CompletionForRegister(apdu)

            _terminalOperationState.value = if (abortOperationTriggered) {
                moveToFront()
                State.OperationAborted
            } else {
                moveToFront()
                State.Success(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = completion,
                    data = LavegoTransactionData(
                        additionalText = "",
                        aid = "",
                        amount = BigInteger.ZERO,
                        cardName = "",
                        cardSeqNumber = 0,
                        cardType = 0,
                        chipData = "",
                        data = "",
                        date = "",
                        expiry = "",
                        receiptNo = 0,
                        resultCode = 0,
                        resultText = "",
                        singleAmounts = "",
                        tags = emptyMap(),
                        tid = status.tid(),
                        time = "",
                        trace = "",
                        traceOrig = "",
                        track1 = "",
                        track2 = "",
                        track3 = "",
                        vu = ""
                    )
                )
            }
        }
    }

    internal sealed class State {
        data object Idle : State()
        data object Setup : State()
        data object Operation : State()
        data class Error(
            private val date: Instant,
            private val customerReceipt: String,
            private val merchantReceipt: String,
            private val rawData: String,
            private val data: LavegoTransactionData?,
            private val resultCode: TransactionResultCode
        ) : State() {
            private val terminalOperation = TerminalOperationError(
                date = date,
                customerReceipt = customerReceipt,
                merchantReceipt = merchantReceipt,
                rawData = rawData,
                data = data?.let {
                    TransactionData(
                        terminalType = ZvtTerminal.TYPE,
                        terminalId = it.tid,
                        transactionId = it.receiptNo.toString(),
                        cardCircuit = it.cardName,
                        cardPan = it.chipData,
                        paymentProvider = it.additionalText
                    )
                },
                resultCode = resultCode,
                isRecoverable = false
            )
            val payment: TerminalOperationStatus.Payment.Error by lazy {
                TerminalOperationStatus.Payment.Error(terminalOperation)
            }
            val refund: TerminalOperationStatus.Refund.Error by lazy {
                TerminalOperationStatus.Refund.Error(terminalOperation)
            }
            val reversal: TerminalOperationStatus.Reversal.Error by lazy {
                TerminalOperationStatus.Reversal.Error(terminalOperation)
            }
            val connect: TerminalOperationStatus.Login.Error by lazy {
                TerminalOperationStatus.Login.Error(date, rawData, resultCode)
            }
            val reconciliation: TerminalOperationStatus.Reconciliation.Error by lazy {
                TerminalOperationStatus.Reconciliation.Error(terminalOperation)
            }
        }
        data class Success(
            private val date: Instant,
            private val customerReceipt: String,
            private val merchantReceipt: String,
            private val rawData: String,
            private val data: LavegoTransactionData?
        ) : State() {
            private val terminalOperation = TerminalOperationSuccess(
                date = date,
                customerReceipt = customerReceipt,
                merchantReceipt = merchantReceipt,
                rawData = rawData,
                data = data?.let {
                    TransactionData(
                        terminalType = ZvtTerminal.TYPE,
                        terminalId = it.tid,
                        transactionId = it.receiptNo.toString(),
                        cardCircuit = it.cardName,
                        cardPan = it.chipData,
                        paymentProvider = it.additionalText
                    )
                }
            )
            val payment: TerminalOperationStatus.Payment.Success by lazy {
                TerminalOperationStatus.Payment.Success(terminalOperation)
            }
            val refund: TerminalOperationStatus.Refund.Success by lazy {
                TerminalOperationStatus.Refund.Success(terminalOperation)
            }
            val reversal: TerminalOperationStatus.Reversal.Success by lazy {
                TerminalOperationStatus.Reversal.Success(terminalOperation)
            }
            val connect: TerminalOperationStatus.Login.Connected by lazy {
                TerminalOperationStatus.Login.Connected(
                    date = date,
                    rawData = rawData,
                    terminalType = ZvtTerminal.TYPE,
                    terminalId = data?.tid.orEmpty()
                )
            }
            val reconciliation: TerminalOperationStatus.Reconciliation.Success by lazy {
                TerminalOperationStatus.Reconciliation.Success(terminalOperation)
            }
        }

        data object OperationAborted : State()
    }
}
