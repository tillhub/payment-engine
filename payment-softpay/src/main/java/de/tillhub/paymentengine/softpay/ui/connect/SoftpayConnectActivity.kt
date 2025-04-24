package de.tillhub.paymentengine.softpay.ui.connect

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import de.tillhub.paymentengine.softpay.ui.SoftpayTerminalActivity
import io.softpay.sdk.failure.Failure

internal class SoftpayConnectActivity : SoftpayTerminalActivity() {

    private val viewModel by viewModels<SoftpayConnectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.init(softpay.loginManager)
    }

    override fun showLoader() {
        binding.loader.isVisible = true
    }

    override fun startOperation() {
        viewModel.login(
            username = config.config.merchantUsername,
            password = config.config.merchantPassword
        )
    }
}

