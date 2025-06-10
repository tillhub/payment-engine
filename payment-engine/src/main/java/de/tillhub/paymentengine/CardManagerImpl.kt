package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class CardManagerImpl(
    protected val configs: MutableMap<String, Terminal> = mutableMapOf(),
    protected val terminalState: MutableStateFlow<TerminalOperationStatus> = MutableStateFlow(
        TerminalOperationStatus.Waiting
    )
) : CardManager {

    internal val defaultConfig: Terminal by lazy {
        ZvtTerminal.create()
    }

    override fun putTerminalConfig(config: Terminal) {
        configs[config.id] = config
    }
}