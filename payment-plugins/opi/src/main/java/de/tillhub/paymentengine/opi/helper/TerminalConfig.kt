package de.tillhub.paymentengine.opi.helper

import java.time.Instant

internal interface TerminalConfig {
    fun timeNow(): Instant
}

internal class TerminalConfigImpl : TerminalConfig {
    override fun timeNow(): Instant {
        return Instant.now()
    }
}