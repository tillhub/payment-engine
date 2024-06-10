package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal abstract class CardManagerImpl(
    protected val configs: MutableMap<String, Terminal> = mutableMapOf(),
    protected val terminalState: MutableStateFlow<TerminalOperationStatus> = MutableStateFlow(
        TerminalOperationStatus.Waiting
    )
) : CardManager {

    protected val defaultConfig: Terminal by lazy {
        Terminal.ZVT()
    }

    override fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    override fun observePaymentState(): StateFlow<TerminalOperationStatus> = terminalState
}