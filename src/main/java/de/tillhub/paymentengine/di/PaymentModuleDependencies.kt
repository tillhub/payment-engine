package de.tillhub.paymentengine.di

import com.squareup.moshi.Moshi
import de.tillhub.paymentengine.providers.PaymentTime
import kotlinx.coroutines.CoroutineScope

interface PaymentModuleDependencies {
    fun moshi(): Moshi
    fun paymentTime(): PaymentTime
    fun coroutineScope(): CoroutineScope
}