package de.tillhub.paymentengine.softpay

import android.app.Application
import de.tillhub.paymentengine.softpay.data.SoftpayConfig
import de.tillhub.paymentengine.softpay.helpers.FlowListenerImpl
import io.softpay.sdk.Softpay
import io.softpay.sdk.SoftpayFactory
import io.softpay.sdk.SoftpayOptions
import io.softpay.sdk.SoftpayProvider
import io.softpay.sdk.domain.Integrator
import io.softpay.sdk.meta.ExperimentalSoftpayApi

abstract class SoftpayApplication : Application(), SoftpayProvider  {
    private lateinit var config: SoftpayConfig

    fun setConfig(config: SoftpayConfig) {
        this.config = config
    }

    @ExperimentalSoftpayApi
    private val softpay: Softpay by lazy {
        SoftpayFactory.getOrCreate {
            val integrator = Integrator.Builder()
                .integrator(config.integratorId)
                .access(
                    accessId = config.accessId,
                    accessSecret = config.accessSecret.toCharArray()
                )
                .build()

            SoftpayOptions.Builder()
                .context(this)
                .integrator(integrator)
                .flags(debug = true)
                .flowListener(FlowListenerImpl())
                .build()
        }
    }

    @ExperimentalSoftpayApi
    override fun softpay(): Softpay = softpay
}