package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.StateFlow

interface CardManager {
    fun putTerminalConfig(config: Terminal)
    fun observePaymentState(): StateFlow<TerminalOperationStatus>
}