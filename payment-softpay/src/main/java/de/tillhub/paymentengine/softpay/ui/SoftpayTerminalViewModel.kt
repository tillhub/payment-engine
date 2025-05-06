package de.tillhub.paymentengine.softpay.ui

import androidx.lifecycle.ViewModel
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.R
import de.tillhub.paymentengine.softpay.helpers.TerminalConfig
import de.tillhub.paymentengine.softpay.helpers.TerminalConfigImpl
import de.tillhub.paymentengine.softpay.helpers.activeFlow
import de.tillhub.paymentengine.softpay.helpers.toTransactionResultCode
import io.softpay.sdk.failure.Failure
import io.softpay.sdk.failure.failure
import io.softpay.sdk.failure.failureOf
import io.softpay.sdk.flow.component1
import io.softpay.sdk.flow.component2
import io.softpay.sdk.flow.inputArgs2
import io.softpay.sdk.login.LoginFlow
import io.softpay.sdk.login.LoginFlowInput
import io.softpay.sdk.login.LoginFlowInput.CredentialsInput
import io.softpay.sdk.login.LoginFlowModel.Update
import io.softpay.sdk.login.LoginFlowModel.Update.InputRequest
import io.softpay.sdk.login.LoginFlowOptions
import io.softpay.sdk.login.LoginFlowReceiver
import io.softpay.sdk.login.LoginFlowVariant
import io.softpay.sdk.login.LoginManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

internal class SoftpayTerminalViewModel(
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    private lateinit var loginFlow: LoginFlow

    private val loginReceiver = LoginFlowReceiver { model ->
        // Invoked on a worker thread, never on the main thread.
        model.update()?.let { update ->
            when (update) {
                // Some Input is requested.
                is InputRequest -> when {
                    // Merchant Credentials are requested.
                    model.awaits(CredentialsInput::class.java) -> {
                        val (reason, failure) = model.inputArgs2<CredentialsInput.Reason, Failure?>()
                        _state.value = when (reason) {
                            CredentialsInput.Reason.FIRST -> LoginState.CredentialsInput
                            CredentialsInput.Reason.INVALID -> {
                                LoginState.Error.WrongCredentials(
                                    date = terminalConfig.timeNow(),
                                    resultCode = TransactionResultCode.Known(
                                        errorMessage = R.string.softpay_error_wrong_credentials,
                                        recoveryMessages = listOf(
                                            R.string.softpay_recovery_wrong_credentials,
                                            R.string.softpay_recovery_contact_support
                                        )
                                    ),
                                )
                            }
                            CredentialsInput.Reason.FAILURE -> failure.failure()?.let {
                                LoginState.Error.General(
                                    date = terminalConfig.timeNow(),
                                    resultCode = it.toTransactionResultCode(),
                                    failure = it
                                )
                            } ?: LoginState.Error.General(
                                date = terminalConfig.timeNow(),
                                resultCode = TransactionResultCode.Known(
                                    errorMessage = R.string.softpay_error_login_failure,
                                    recoveryMessages = listOf(
                                        R.string.softpay_recovery_contact_support,
                                    )
                                ),
                                failure = failureOf("Login failed")
                            )
                        }
                    }
                    model.awaits(LoginFlowInput.UnlockTokenInput::class.java) -> {
                        // TODO handle UnlockTokenInput
                        null
                    }
                    else -> null
                }
                else -> {
                    when {
                        // SDK is either attesting or still processing.
                        model.state.attesting || model.state.processing -> {
                            _state.value = LoginState.Loading
                        }
                        // Final state: Login failed.
                        model.state.failure -> {
                            val failureUpdate = update as Update.Failure
                            _state.value = LoginState.Error.General(
                                date = terminalConfig.timeNow(),
                                resultCode = failureUpdate.failure.toTransactionResultCode(),
                                failure = failureUpdate.failure
                            )
                        }

                        // Final state: Login was successful.
                        model.state.success -> {
                            _state.value = LoginState.LoggedIn
                        }

                        else -> null
                    }
                }
            }
        }
        true
    }

    fun init(loginManager: LoginManager) {
        if (loginManager.authenticated) {
            _state.value = LoginState.LoggedIn
        } else {
            _state.value = LoginState.Loading
            setupLoginFlow(loginManager)
        }
    }

    fun login(username: String, password: String) {
        loginFlow.dispatch(
            CredentialsInput(
                username = username.toCharArray(),
                password = password.toCharArray()
            )
        )
    }

    private fun setupLoginFlow(loginManager: LoginManager) {
        loginFlow = loginManager.activeFlow ?: run {
            val variant = if (loginManager.locked) {
                LoginFlowVariant.UNLOCK
            } else {
                LoginFlowVariant.LOGIN
            }
            val options = LoginFlowOptions.of(variant = variant)
            loginManager.newFlow(options)
        }

        loginFlow.subscribe(loginReceiver)
    }

    override fun onCleared() {
        super.onCleared()
        loginFlow.unsubscribe()
    }
}

internal sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object CredentialsInput : LoginState()

    sealed class Error : LoginState() {
        abstract val date: Instant
        abstract val resultCode: TransactionResultCode

        data class WrongCredentials(
            override val date: Instant,
            override val resultCode: TransactionResultCode
        ) : Error()
        data class General(
            override val date: Instant,
            override val resultCode: TransactionResultCode,
            val failure: Failure
        ) : Error()
    }
    data object LoggedIn : LoginState()
}