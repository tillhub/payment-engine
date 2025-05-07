package de.tillhub.paymentengine.softpay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.R
import de.tillhub.paymentengine.softpay.helpers.TerminalConfig
import de.tillhub.paymentengine.softpay.helpers.TerminalConfigImpl
import de.tillhub.paymentengine.softpay.helpers.activeFlow
import de.tillhub.paymentengine.softpay.helpers.toTransactionResultCode
import io.softpay.sdk.config.ConfigFlow
import io.softpay.sdk.config.ConfigFlowInput.AppAuthorizeConfirmation
import io.softpay.sdk.config.ConfigFlowInput.PinInput
import io.softpay.sdk.config.ConfigFlowInput.StoreInput
import io.softpay.sdk.config.ConfigFlowModel
import io.softpay.sdk.config.ConfigFlowModel.State.ABORTED
import io.softpay.sdk.config.ConfigFlowOptions
import io.softpay.sdk.config.ConfigFlowReceiver
import io.softpay.sdk.config.ConfigFlowVariant
import io.softpay.sdk.config.ConfigManager
import io.softpay.sdk.config.dispatch
import io.softpay.sdk.domain.Store
import io.softpay.sdk.failure.Failure
import io.softpay.sdk.failure.failure
import io.softpay.sdk.failure.failureOf
import io.softpay.sdk.flow.component1
import io.softpay.sdk.flow.component2
import io.softpay.sdk.flow.component3
import io.softpay.sdk.flow.component4
import io.softpay.sdk.flow.inputArg
import io.softpay.sdk.flow.inputArgs2
import io.softpay.sdk.flow.inputArgs4
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
import kotlinx.coroutines.launch
import java.time.Instant

internal class SoftpayTerminalViewModel(
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    private lateinit var loginFlow: LoginFlow
    private lateinit var configFlow: ConfigFlow

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
                is ConfigFlowModel.Update.InputRequest -> null
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
                            resultCode = TransactionResultCode.Known(
                                errorMessage = R.string.softpay_error_login_failure,
                                recoveryMessages = listOf(
                                    R.string.softpay_recovery_contact_support,
                                )
                            ),
                            failure = failureUpdate.failure
                        )
                    }
                    else -> null
                }
            }
        }
        true
    }

    fun initLogin(loginManager: LoginManager) {
        if (loginManager.authenticated) {
            _state.value = LoginState.LoggedIn
        } else {
            _state.value = LoginState.Loading
            setupLoginFlow(loginManager)
        }
    }

    fun initStoreConfiguration(configManager: ConfigManager, storeId: String) {
        if (configManager.configured) {
            _state.value = LoginState.StoreConfigured
        } else {
            _state.value = LoginState.Loading

            setupStoreConfigFlow(configManager)

            configManager.getStoreByAcquirerStoreId(storeId) { store, failure ->
                if (failure == null && store != null) {
                    configFlow.dispatch(StoreInput.SelectStore(store))
                } else {
                    _state.value = LoginState.Error.General(
                        date = terminalConfig.timeNow(),
                        resultCode = TransactionResultCode.Known(
                            errorMessage = R.string.softpay_error_store_not_found,
                            recoveryMessages = listOf(
                                R.string.softpay_recovery_contact_support,
                            )
                        ),
                        failure = failure ?: failureOf("Store not found")
                    )
                }
            }
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

    private fun setupStoreConfigFlow(configManager: ConfigManager) {
        configFlow = configManager.activeFlow ?: run {
            val options = ConfigFlowOptions.of(variant = ConfigFlowVariant.INSTALL)
            configManager.newFlow(options)
        }

        configFlow.subscribe(configReceiver)
    }

    override fun onCleared() {
        super.onCleared()

        if (::loginFlow.isInitialized) {
            loginFlow.unsubscribe()
        }
        if (::configFlow.isInitialized) {
            configFlow.unsubscribe()
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
    data object StoreConfigured : LoginState()
}