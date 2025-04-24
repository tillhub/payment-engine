package de.tillhub.paymentengine.softpay.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
import de.tillhub.paymentengine.softpay.databinding.ActivityTerminalBinding
import de.tillhub.paymentengine.softpay.helpers.FlowListenerImpl
import de.tillhub.paymentengine.softpay.helpers.viewBinding
import io.softpay.sdk.LogOptions
import io.softpay.sdk.Softpay
import io.softpay.sdk.SoftpayFactory
import io.softpay.sdk.SoftpayOptions
import io.softpay.sdk.SoftpayProvider
import io.softpay.sdk.domain.Integrator
import io.softpay.sdk.meta.DelicateSoftpayApi

internal abstract class SoftpayTerminalActivity : AppCompatActivity(), SoftpayProvider {

    protected val binding by viewBinding(ActivityTerminalBinding::inflate)

    protected val config: SoftpayTerminal by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, SoftpayTerminal::class.java)
        } ?: throw IllegalArgumentException("SoftpayTerminalActivity: Extras is null")
    }

    @DelicateSoftpayApi
    protected val softpay: Softpay by lazy {
        SoftpayFactory.getOrCreate {
            val integrator = Integrator.Builder()
                .access(
                    accessId = config.config.accessId,
                    accessSecret = config.config.accessId.toCharArray()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun softpay(): Softpay? = softpay

    abstract fun showLoader()
    abstract fun startOperation()
}