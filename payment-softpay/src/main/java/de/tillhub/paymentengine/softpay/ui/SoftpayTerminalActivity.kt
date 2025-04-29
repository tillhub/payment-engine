package de.tillhub.paymentengine.softpay.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.softpay.SoftpayApplication
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
import de.tillhub.paymentengine.softpay.databinding.ActivityTerminalBinding
import de.tillhub.paymentengine.softpay.helpers.viewBinding
import io.softpay.sdk.Softpay
import io.softpay.sdk.meta.ExperimentalSoftpayApi

@ExperimentalSoftpayApi
internal abstract class SoftpayTerminalActivity : AppCompatActivity() {

    protected val binding by viewBinding(ActivityTerminalBinding::inflate)

    protected val config: SoftpayTerminal by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, SoftpayTerminal::class.java)
        } ?: throw IllegalArgumentException("SoftpayTerminalActivity: Extras is null")
    }

    protected val softpay: Softpay by lazy {
        require(application is SoftpayApplication) { "Application type is not SoftpayApplication" }

        (application as SoftpayApplication).let {
            it.setConfig(config.config)
            it.softpay()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }


}