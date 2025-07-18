package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TicketReprintContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.time.Instant

/**
 * This is called to try to initiate reprint of last receipt
 */
interface TicketReprintManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.TicketReprint>
    fun startTicketReprint()
    fun startTicketReprint(configId: String)
    fun startTicketReprint(config: Terminal)
}

internal class TicketReprintManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val ticketReprintContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TicketReprintContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), TicketReprintManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.TicketReprint> =
        terminalState.filterIsInstance(TerminalOperationStatus.TicketReprint::class)

    override fun startTicketReprint() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startTicketReprint(configId)
    }

    override fun startTicketReprint(configId: String) {
        val terminalConfig = configs[configId]
        requireNotNull(terminalConfig) { "Terminal config not found for id: $configId" }
        startTicketReprint(terminalConfig)
    }

    override fun startTicketReprint(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.TicketReprint.Pending)
        try {
            ticketReprintContract.launch(config)
        } catch (_: UnsupportedOperationException) {
            terminalState.tryEmit(
                TerminalOperationStatus.TicketReprint.Error(
                    date = Instant.now(),
                    resultCode = TransactionResultCode.ACTION_NOT_SUPPORTED
                )
            )
        }
    }
}