package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class CardManagerImpl(
    protected val configs: MutableMap<String, Terminal> = mutableMapOf(),
    protected val terminalState: MutableStateFlow<TerminalOperationStatus> = MutableStateFlow(
        TerminalOperationStatus.Waiting
    )
) : CardManager {

    override fun putTerminalConfig(config: Terminal) {
        configs[config.id] = config
    }
}