package de.tillhub.paymentengine.providers

import java.time.Instant

interface PaymentTime {
    fun now(): Instant
}