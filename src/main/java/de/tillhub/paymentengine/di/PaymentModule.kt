package de.tillhub.paymentengine.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import de.tillhub.paymentengine.TerminalManager
import de.tillhub.paymentengine.TerminalManagerImpl
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter

@Module
@InstallIn(ActivityComponent::class)
object PaymentModule {

    @Provides
    fun provideCardPaymentManager(
        paymentTime: PaymentTime,
        lavegoTransactionDataConverter: LavegoTransactionDataConverter,
    ): TerminalManager {
        return TerminalManagerImpl(paymentTime, lavegoTransactionDataConverter)
    }
}