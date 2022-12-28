package de.tillhub.paymentengine.di

import com.squareup.moshi.Moshi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PaymentModuleDependencies {
    fun moshi(): Moshi
    fun paymentTime(): PaymentTime
}