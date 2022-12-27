package de.tillhub.paymentengine.di

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope

interface PaymentModuleDependencies {
    fun moshi(): Moshi
    fun paymentTime(): PaymentTime
    fun coroutineScope(): CoroutineScope
}