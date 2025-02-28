package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.Flow

interface CardManager {
    fun putTerminalConfig(config: Terminal)
    fun observePaymentState(): Flow<TerminalOperationStatus>
}