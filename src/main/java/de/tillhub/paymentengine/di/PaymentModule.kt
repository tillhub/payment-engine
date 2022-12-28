package de.tillhub.paymentengine.di

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import de.tillhub.paymentengine.TerminalManager
import de.tillhub.paymentengine.TerminalManagerImpl
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter

@Module
@DisableInstallInCheck
object PaymentModule {

    @Provides
    @PaymentScope
    fun provideCardPaymentManager(
        paymentTime: PaymentTime,
        lavegoTransactionDataConverter: LavegoTransactionDataConverter,
    ): TerminalManager {
        return TerminalManagerImpl(paymentTime, lavegoTransactionDataConverter)
    }
}