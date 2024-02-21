package de.tillhub.paymentengine

import androidx.lifecycle.DefaultLifecycleObserver
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.SharedFlow

interface CardManager : DefaultLifecycleObserver {
    fun putTerminalConfig(config: Terminal)
    fun observePaymentState(): SharedFlow<TerminalOperationStatus>
}