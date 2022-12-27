package de.tillhub.paymentengine.di

import java.time.Instant

interface PaymentTime {
    fun now(): Instant
}