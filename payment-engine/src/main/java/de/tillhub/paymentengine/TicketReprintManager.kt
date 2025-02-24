package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TicketReprintContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

/**
 * This is called to try to initiate reprint of last receipt
 */
interface TicketReprintManager : CardManager {
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

    override fun startTicketReprint() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startTicketReprint(configId)
    }

    override fun startTicketReprint(configId: String) {
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startTicketReprint(terminalConfig)
    }

    override fun startTicketReprint(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.TicketReprint)
        try {
            ticketReprintContract.launch(config)
        } catch (_: UnsupportedOperationException) {
            terminalState.tryEmit(
                when (config) {
                    is Terminal.OPI -> TerminalOperationStatus.Error.OPI(
                        date = Instant.now(),
                        resultCode = ResultCodeSets.ACTION_NOT_SUPPORTED
                    )
                    is Terminal.SPOS -> TerminalOperationStatus.Error.SPOS(
                        date = Instant.now(),
                        resultCode = ResultCodeSets.ACTION_NOT_SUPPORTED,
                        isRecoverable = false
                    )
                    is Terminal.ZVT -> TerminalOperationStatus.Error.ZVT(
                        date = Instant.now(),
                        resultCode = ResultCodeSets.ACTION_NOT_SUPPORTED
                    )
                }
            )
        } catch (_: ActivityNotFoundException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Error.SPOS(
                    date = Instant.now(),
                    resultCode = ResultCodeSets.APP_NOT_FOUND_ERROR,
                    isRecoverable = false
                )
            )
        }
    }
}