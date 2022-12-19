package de.tillhub.paymentengine.di

import android.content.Context
import com.squareup.moshi.Moshi
import de.tillhub.paymentengine.CardPaymentConfig
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardPaymentManagerImpl
import de.tillhub.paymentengine.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import de.tillhub.paymentengine.providers.PaymentTime


object PaymentModule {

    fun provideCardPaymentManager(
        context: Context,
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

    fun provideCardPaymentConfig() = CardPaymentConfig()

    fun provideCardSaleConfig() = CardSaleConfig()
}