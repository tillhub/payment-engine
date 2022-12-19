package de.tillhub.paymentengine.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CardTerminalViewModel : ViewModel() {

    private val _terminalOperationState: MutableLiveData<TerminalOperationState> =
        MutableLiveData(TerminalOperationState.Idle)
    val terminalOperationState: LiveData<TerminalOperationState> = _terminalOperationState

    fun init() {
        _terminalOperationState.value = TerminalOperationState.Setup
    }

    fun onOperationCompleted() {
        _terminalOperationState.value = when (terminalOperationState.value) {
            TerminalOperationState.Setup -> TerminalOperationState.Operation
            else -> TerminalOperationState.Done
        }
    }
}

sealed class TerminalOperationState {
    object Idle : TerminalOperationState()
    object Setup : TerminalOperationState()
    object Operation : TerminalOperationState()
    object Done : TerminalOperationState()
}
