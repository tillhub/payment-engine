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
                .merchant("Testpay") // TODO remove
                .access(
                    accessId = config.accessId,
                    accessSecret = config.accessSecret.toCharArray()
                )
                .external(externalSecret = "73658FA522AC4A0FA108D1E0A191B715".toCharArray())  // TODO remove
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