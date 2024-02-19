package de.tillhub.paymentengine.helper

import java.time.Instant

interface TerminalConfig {
    fun timeNow(): Instant
}

class TerminalConfigImpl : TerminalConfig {
    override fun timeNow(): Instant {
        return Instant.now()
    }
}