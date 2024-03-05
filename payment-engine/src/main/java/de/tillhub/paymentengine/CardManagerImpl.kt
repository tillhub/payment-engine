package de.tillhub.paymentengine

import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class CardManagerImpl(
    protected val configs: MutableMap<String, Terminal> = mutableMapOf(),
    protected val terminalState: MutableStateFlow<TerminalOperationStatus> = MutableStateFlow(
        TerminalOperationStatus.Waiting
    )
) : CardManager {

    override fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    override fun observePaymentState(): StateFlow<TerminalOperationStatus> = terminalState

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerResultRegistry(owner)
    }

    abstract fun registerResultRegistry(owner: LifecycleOwner)
}