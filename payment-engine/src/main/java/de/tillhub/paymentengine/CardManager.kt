package de.tillhub.paymentengine

import androidx.lifecycle.DefaultLifecycleObserver
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.StateFlow

interface CardManager : DefaultLifecycleObserver {
    fun putTerminalConfig(config: Terminal)
    fun observePaymentState(): StateFlow<TerminalOperationStatus>
}