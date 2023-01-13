package de.tillhub.paymentengine.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.tillhub.paymentengine.CardPaymentConfigRepository
import de.tillhub.paymentengine.CardSaleConfigRepository
import de.tillhub.paymentengine.TerminalConfig

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PaymentModuleDependencies {
    fun terminalConfig(): TerminalConfig
    fun cardPaymentConfigRepository(): CardPaymentConfigRepository
    fun cardSaleConfigRepository(): CardSaleConfigRepository
}