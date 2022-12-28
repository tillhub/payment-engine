package de.tillhub.paymentengine.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.tillhub.paymentengine.TerminalManager
import de.tillhub.paymentengine.TerminalManagerImpl
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideTerminalManager(
        paymentTime: PaymentTime,
        lavegoTransactionDataConverter: LavegoTransactionDataConverter,
    ): TerminalManager {
        return TerminalManagerImpl(paymentTime, lavegoTransactionDataConverter)
    }
}