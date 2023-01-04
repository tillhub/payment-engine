package de.tillhub.paymentengine.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.tillhub.paymentengine.CardPaymentConfigRepository
import de.tillhub.paymentengine.CardSaleConfigRepository
import de.tillhub.paymentengine.TerminalTime

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PaymentModuleDependencies {
    fun terminalTime(): TerminalTime
    fun cardPaymentConfigRepository(): CardPaymentConfigRepository
    fun cardSaleConfigRepository(): CardSaleConfigRepository
}