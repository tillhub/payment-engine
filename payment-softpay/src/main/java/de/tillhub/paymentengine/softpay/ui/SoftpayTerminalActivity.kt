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
import io.softpay.sdk.Softpay
import io.softpay.sdk.meta.ExperimentalSoftpayApi

@ExperimentalSoftpayApi
internal abstract class SoftpayTerminalActivity : AppCompatActivity() {

    private val loginViewModel by viewModels<SoftpayTerminalViewModel>()

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

        loginViewModel.state.collectWithOwner(this) { state ->
            when (state) {
                LoginState.Idle,
                LoginState.Loading -> showLoader()

                LoginState.CredentialsInput -> startLogin()
                LoginState.LoggedIn -> startOperation()

                is LoginState.Error -> showError(state)
            }
        }

        loginViewModel.init(softpay.loginManager)
    }

    private fun startLogin() {
        loginViewModel.login(
            username = config.config.merchantUsername,
            password = config.config.merchantPassword
        )
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