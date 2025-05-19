package de.tillhub.paymentengine.softpay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.R
import de.tillhub.paymentengine.softpay.SoftpayApplication
import de.tillhub.paymentengine.softpay.helpers.TerminalConfig
import de.tillhub.paymentengine.softpay.helpers.TerminalConfigImpl
import de.tillhub.paymentengine.softpay.helpers.activeFlow
import de.tillhub.paymentengine.softpay.helpers.toTransactionResultCode
import io.softpay.sdk.config.ConfigFlow
import io.softpay.sdk.config.ConfigFlowInput.AppAuthorizeConfirmation
import io.softpay.sdk.config.ConfigFlowInput.StoreInput
import io.softpay.sdk.config.ConfigFlowModel
import io.softpay.sdk.config.ConfigFlowModel.State.ABORTED
import io.softpay.sdk.config.ConfigFlowOptions
import io.softpay.sdk.config.ConfigFlowReceiver
import io.softpay.sdk.config.ConfigFlowVariant
import io.softpay.sdk.config.ConfigManager
import io.softpay.sdk.config.dispatch
import io.softpay.sdk.failure.Failure
import io.softpay.sdk.failure.failure
import io.softpay.sdk.failure.failureOf
import io.softpay.sdk.flow.inputArg
import io.softpay.sdk.login.LoginFlow
import io.softpay.sdk.login.LoginFlowInput
import io.softpay.sdk.login.LoginFlowInput.CredentialsInput
import io.softpay.sdk.login.LoginFlowModel
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
    private val loginManager: LoginManager,
    private val configManager: ConfigManager,
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    // Create a LoginFlow in order to authenticate the merchant.
    private val loginFlow: LoginFlow = loginManager.activeFlow ?: run {
        // If the SDK is in a locked state we start the login flow with UNLOCK variant.
        // Otherwise, in LOGIN variant.
        val variant = if (loginManager.locked) {
            LoginFlowVariant.UNLOCK
        } else {
            LoginFlowVariant.LOGIN
        }
        val options = LoginFlowOptions.of(variant = variant)
        loginManager.newFlow(options)
    }
    private val configFlow: ConfigFlow = configManager.activeFlow ?: run {
        val options = ConfigFlowOptions.of(variant = ConfigFlowVariant.INSTALL)
        configManager.newFlow(options)
    }

    private val loginReceiver = LoginFlowReceiver { model: LoginFlowModel ->
        // Invoked on a worker thread, never on the main thread.
        model.update()?.let { update ->
            when (update) {
                // Some Input is requested.
                is InputRequest -> when {
                    // Merchant Credentials are requested.
                    model.awaits(CredentialsInput::class.java) -> {
                        val reason = model.inputArg<CredentialsInput.Reason>(1)
                        val failure = model.inputArg<Failure?>(2)
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
                else -> when {
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
        true
    }
    private val configReceiver = ConfigFlowReceiver { model ->
        model.update()?.let { update ->
            when (update) {
                is ConfigFlowModel.Update.InputRequest -> when {
                    // Is the end-user allowed to configure this device?
                    model.awaits(AppAuthorizeConfirmation::class.java) -> {
                        // Confirm if end-user is allowed to configure this device or not (confirmed = false).
                        model.dispatch(AppAuthorizeConfirmation(confirmed = true))
                    }
                    // Event is spawned when flow waits for store selection
                    model.awaits(StoreInput::class.java) -> {
                        _state.value = LoginState.StoreInput
                    }
                    else -> null
                }
                else -> when {
                    // Aborted by the end-user.
                    model.state == ABORTED -> {
                        _state.value = LoginState.Error.General(
                            date = terminalConfig.timeNow(),
                            resultCode = TransactionResultCode.Known(
                                errorMessage = R.string.softpay_error_login_failure,
                                recoveryMessages = listOf(
                                    R.string.softpay_recovery_contact_support,
                                )
                            ),
                            failure = failureOf("Configuration Aborted")
                        )
                    }
                    // SDK is either attesting or still processing.
                    model.state.attesting || model.state.processing -> {
                        _state.value = LoginState.Loading
                    }
                    // Final state: Configuration was successful.
                    model.state.success -> {
                        _state.value = LoginState.StoreConfigured
                    }
                    // Final state: Configuration failed.
                    model.state.failure -> {
                        val failureUpdate = update as ConfigFlowModel.Update.Failure
                        _state.value = LoginState.Error.General(
                            date = terminalConfig.timeNow(),
                            resultCode = failureUpdate.failure.toTransactionResultCode(),
                            failure = failureUpdate.failure
                        )
                    }
                    else -> null
                }
            }
        }
        true
    }

    fun initLogin() {
        if (loginManager.authenticated) {
            _state.value = LoginState.LoggedIn
        } else {
            _state.value = LoginState.Loading
            loginFlow.subscribe(loginReceiver)
        }
    }

    fun initStoreConfiguration() {
        if (configManager.configured) {
            _state.value = LoginState.StoreConfigured
        } else {
            _state.value = LoginState.Loading

            configFlow.subscribe(configReceiver)
        }
    }

    fun storeSelection(storeId: String) {
        configManager.getStoreByAcquirerStoreId(storeId) { store, failure ->
            if (failure == null && store != null) {
                configFlow.dispatch(StoreInput.SelectStore(store))
            } else {
                _state.value = failure?.let {
                    LoginState.Error.General(
                        date = terminalConfig.timeNow(),
                        resultCode = failure.toTransactionResultCode(),
                        failure = failure
                    )
                } ?: LoginState.Error.General(
                    date = terminalConfig.timeNow(),
                    resultCode = TransactionResultCode.Known(
                        errorMessage = R.string.softpay_error_store_not_found,
                        recoveryMessages = listOf(
                            R.string.softpay_recovery_contact_support,
                        )
                    ),
                    failure = failureOf("Store not found")
                )
            }
        }
    }

    fun merchantLogin(username: String, password: String) {
        loginFlow.dispatch(
            CredentialsInput(
                username = username.toCharArray(),
                password = password.toCharArray()
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (loginManager.subscriptions > 0) {
            loginFlow.unsubscribe()
        }
        if (configManager.subscriptions > 0) {
            configFlow.unsubscribe()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                require(this[APPLICATION_KEY] is SoftpayApplication) {
                    "Application type is not SoftpayApplication"
                }
                val application = (this[APPLICATION_KEY] as SoftpayApplication)
                SoftpayTerminalViewModel(
                    loginManager = application.softpay().loginManager,
                    configManager = application.softpay().configManager,
                )
            }
        }
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
    data object StoreInput : LoginState()
    data object StoreConfigured : LoginState()
}