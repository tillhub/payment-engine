package de.tillhub.paymentengine.softpay.ui.connect

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import de.tillhub.paymentengine.softpay.helpers.collectWithOwner
import de.tillhub.paymentengine.softpay.ui.SoftpayTerminalActivity

internal class SoftpayConnectActivity : SoftpayTerminalActivity() {

    private val viewModel by viewModels<SoftpayConnectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.collectWithOwner(this) { state ->
            when (state) {
                ConnectState.Idle,
                ConnectState.Loading -> showLoader()

                ConnectState.CredentialsInput -> startOperation()

                ConnectState.Error.WrongCredentials -> TODO()
                is ConnectState.Error.General -> TODO()

                ConnectState.Success -> TODO()
            }
        }

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

