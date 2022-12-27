package de.tillhub.paymentengine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardPaymentManagerImpl
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import kotlinx.coroutines.CoroutineScope

@Module
@DisableInstallInCheck
object PaymentModule {

    @Provides
    @PaymentScope
    fun provideCardPaymentManager(
        context: Context,
        lavegoTransactionDataConverter: LavegoTransactionDataConverter,
        paymentTime: PaymentTime,
        coroutineScope: CoroutineScope
    ): CardPaymentManager {
        return CardPaymentManagerImpl(
            context,
            lavegoTransactionDataConverter,
            paymentTime,
            coroutineScope
        )
    }

}