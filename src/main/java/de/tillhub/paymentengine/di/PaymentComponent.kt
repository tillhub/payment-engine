package de.tillhub.paymentengine.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.tillhub.paymentengine.CardPaymentConfig
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.CardSaleConfig
import de.tillhub.paymentengine.ui.CardTerminalActivity

@PaymentScope
@Component(
    dependencies = [
        PaymentModuleDependencies::class
    ],
    modules = [
        PaymentModule::class
    ]
)
interface PaymentComponent {

    fun inject(activity: CardTerminalActivity)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(paymentModuleDependencies: PaymentModuleDependencies): Builder
        fun build(): PaymentComponent
    }

    fun cardPaymentManager(): CardPaymentManager

    fun cardPaymentConfig(): CardPaymentConfig

    fun cardSaleConfig(): CardSaleConfig
}