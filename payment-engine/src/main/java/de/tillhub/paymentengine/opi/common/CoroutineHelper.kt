package de.tillhub.paymentengine.opi.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> withOPIContext(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block)
}