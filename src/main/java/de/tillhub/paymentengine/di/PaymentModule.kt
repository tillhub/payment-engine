package de.tillhub.paymentengine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import de.tillhub.paymentengine.CardPaymentConfig
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardPaymentManagerImpl
import de.tillhub.paymentengine.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import de.tillhub.paymentengine.providers.PaymentTime
import kotlinx.coroutines.CoroutineScope

@Module
@DisableInstallInCheck
class PaymentModule {

    @Provides
    @PaymentScope
    fun provideCardPaymentConfig(): CardPaymentConfig =  CardPaymentConfig()

    @Provides
    @PaymentScope
    fun provideCardSaleConfig(): CardSaleConfig = CardSaleConfig()

    @Provides
    @PaymentScope
    fun provideCardPaymentManager(
        context: Context,
        lavegoTransactionDataConverter: LavegoTransactionDataConverter,
        paymentTime:PaymentTime,
        cardPaymentConfig: CardPaymentConfig,
        cardSaleConfig: CardSaleConfig,
        coroutineScope: CoroutineScope
    ): CardPaymentManager {
        return CardPaymentManagerImpl(
            context,
            lavegoTransactionDataConverter,
            paymentTime,
            cardPaymentConfig,
            cardSaleConfig,
            coroutineScope
        )
    }
}