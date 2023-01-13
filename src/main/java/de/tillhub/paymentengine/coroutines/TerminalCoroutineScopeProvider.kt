package de.tillhub.paymentengine.coroutines

import kotlinx.coroutines.CoroutineScope

interface TerminalCoroutineScopeProvider {
    val terminalScope: CoroutineScope
}