package de.tillhub.paymentengine.di


import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.tillhub.paymentengine.ui.TerminalActivity

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

    fun inject(activity: TerminalActivity)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(paymentModuleDependencies: PaymentModuleDependencies): Builder
        fun build(): PaymentComponent
    }
}