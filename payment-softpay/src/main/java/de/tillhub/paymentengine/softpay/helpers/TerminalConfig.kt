package de.tillhub.paymentengine.softpay.helpers

import java.time.Instant

internal interface TerminalConfig {
    fun timeNow(): Instant
}

internal class TerminalConfigImpl : TerminalConfig {
    override fun timeNow(): Instant {
        return Instant.now()
    }
}