package de.tillhub.paymentengine.softpay

import android.app.Application
import de.tillhub.paymentengine.softpay.helpers.FlowListenerImpl
import io.softpay.sdk.LogOptions
import io.softpay.sdk.Softpay
import io.softpay.sdk.SoftpayFactory
import io.softpay.sdk.SoftpayOptions
import io.softpay.sdk.SoftpayProvider
import io.softpay.sdk.domain.Integrator

abstract class SoftpayApplication : Application(), SoftpayProvider  {

    private val softpay: Softpay by lazy {
        SoftpayFactory.getOrCreate {
            val integrator = Integrator.Builder()
                .integrator(BuildConfig.INTEGRATOR_ID)
                .access(
                    accessId = BuildConfig.ACCESS_ID,
                    accessSecret =  BuildConfig.ACCESS_SECRET.toCharArray()
                )
                .build()

            SoftpayOptions.Builder()
                .context(this)
                .integrator(integrator)
                .flags(debug = true)
                .logOptions(object: LogOptions {
                    override val logLevel = android.util.Log.DEBUG
                })
                .flowListener(FlowListenerImpl())
                .build()
        }
    }


    override fun softpay(): Softpay = softpay
}