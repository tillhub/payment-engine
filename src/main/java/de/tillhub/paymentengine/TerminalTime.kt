package de.tillhub.paymentengine

import java.time.Instant

interface TerminalTime {
    fun now(): Instant
}