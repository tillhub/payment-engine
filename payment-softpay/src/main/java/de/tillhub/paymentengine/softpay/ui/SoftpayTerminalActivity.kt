package de.tillhub.paymentengine.softpay.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.softpay.SoftpayApplication
import de.tillhub.paymentengine.softpay.data.SoftpayTerminal
import de.tillhub.paymentengine.softpay.databinding.ActivityTerminalBinding
import de.tillhub.paymentengine.softpay.helpers.collectWithOwner
import de.tillhub.paymentengine.softpay.helpers.viewBinding

internal abstract class SoftpayTerminalActivity : AppCompatActivity() {

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
        require(application is SoftpayApplication) { "Application type is not SoftpayApplication" }

        setContentView(binding.root)

        loginViewModel.state.collectWithOwner(this) { state ->
            when (state) {
                LoginState.Idle,
                LoginState.Loading -> showLoader()

                LoginState.CredentialsInput -> startLogin()

                is LoginState.Error -> showError(state)

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

    private fun showError(error: LoginState.Error) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Login.Error(
                        date = error.date,
                        rawData = "",
                        resultCode = error.resultCode
                    )
                )
            }
        )
        finish()
    }

    abstract fun startOperation()
}