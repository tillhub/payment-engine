package de.tillhub.paymentengine.di

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.tillhub.paymentengine.CardPaymentConfig
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardPaymentManagerImpl
import de.tillhub.paymentengine.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import de.tillhub.paymentengine.providers.PaymentTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideCardPaymentManager(
        @ApplicationContext context: Context,
        moshi: Moshi,
        paymentTime: PaymentTime,
        cardPaymentConfig: CardPaymentConfig,
        cardSaleConfig: CardSaleConfig
    ): CardPaymentManager {
        return CardPaymentManagerImpl(
            appContext = context,
            lavegoTransactionDataConverter = LavegoTransactionDataConverter(moshi),
            paymentTime = paymentTime,
            cardPaymentConfig = cardPaymentConfig,
            cardSaleConfig = cardSaleConfig
        )
    }

    @Provides
    @Singleton
    fun provideCardPaymentConfig() = CardPaymentConfig()

    @Provides
    @Singleton
    fun provideCardSaleConfig() = CardSaleConfig()
}