package de.tillhub.paymentengine

import kotlinx.coroutines.CoroutineScope
import java.time.Instant

interface TerminalConfig {
    fun timeNow(): Instant
    val terminalScope: CoroutineScope
}