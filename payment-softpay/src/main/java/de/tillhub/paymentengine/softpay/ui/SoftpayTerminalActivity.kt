package de.tillhub.paymentengine.softpay.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
import de.tillhub.paymentengine.softpay.databinding.ActivityTerminalBinding
import de.tillhub.paymentengine.softpay.helpers.collectWithOwner
import de.tillhub.paymentengine.softpay.helpers.viewBinding
import java.time.Instant

internal abstract class SoftpayTerminalActivity : ComponentActivity() {

    private val loginViewModel by viewModels<SoftpayTerminalViewModel> {
        SoftpayTerminalViewModel.Factory
    }
    protected val binding by viewBinding(ActivityTerminalBinding::inflate)

    protected val config: SoftpayTerminal by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, SoftpayTerminal::class.java)
        } ?: throw IllegalArgumentException("SoftpayTerminalActivity: Extras is null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loginViewModel.state.collectWithOwner(this) { state ->
            when (state) {
                LoginState.Idle,
                LoginState.Loading -> showLoader()

                LoginState.CredentialsInput -> startLogin()

                is LoginState.Error -> handleError(state)

                LoginState.LoggedIn -> startStoreConfig()
                LoginState.StoreConfigured -> startOperation()
            }
        }

        loginViewModel.initLogin()
    }

    private fun startLogin() {
        loginViewModel.merchantLogin(
            username = config.config.merchantUsername,
            password = config.config.merchantPassword
        )
    }

    private fun startStoreConfig() {
        loginViewModel.initStoreConfiguration(config.config.storeId)
    }

    private fun showLoader() {
        binding.loader.isVisible = true
    }

    private fun handleError(error: LoginState.Error) =
        handleError(error.date, error.resultCode)

    abstract fun startOperation()
    abstract fun handleError(date: Instant, resultCode: TransactionResultCode)
}