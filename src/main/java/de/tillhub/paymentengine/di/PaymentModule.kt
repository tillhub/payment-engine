package de.tillhub.paymentengine.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardPaymentManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentModule {

    @Binds
    @Singleton
    abstract fun bindCardPaymentManager(cardPaymentManagerImpl: CardPaymentManagerImpl): CardPaymentManager
}