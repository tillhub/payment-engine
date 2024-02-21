package de.tillhub.paymentengine

import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class CardManagerImpl : CardManager {

    protected val configs: MutableMap<String, Terminal> = mutableMapOf()
    protected val transactionState = MutableSharedFlow<TerminalOperationStatus>(extraBufferCapacity = 1)

    override fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    override fun observePaymentState(): SharedFlow<TerminalOperationStatus> = transactionState

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerResultRegistry(owner)
    }

    abstract fun registerResultRegistry(owner: LifecycleOwner)
}