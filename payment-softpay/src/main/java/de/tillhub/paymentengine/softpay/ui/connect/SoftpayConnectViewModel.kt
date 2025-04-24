package de.tillhub.paymentengine.softpay.ui.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tillhub.paymentengine.softpay.helpers.activeFlow
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
import kotlinx.coroutines.launch

internal class SoftpayConnectViewModel : ViewModel()  {

    private val _state = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val state: StateFlow<ConnectState> = _state

    private lateinit var flow: LoginFlow

    private val receiver = LoginFlowReceiver { model ->
        // Invoked on a worker thread, never on the main thread.
        model.update()?.let { update ->
            when (update) {
                // Some Input is requested.
                is InputRequest -> when {
                    // Merchant Credentials are requested.
                    model.awaits(CredentialsInput::class.java) -> {
                        val (reason, failure) = model.inputArgs2<CredentialsInput.Reason,Failure?>()
                        _state.value = when (reason) {
                            CredentialsInput.Reason.FIRST -> ConnectState.CredentialsInput
                            CredentialsInput.Reason.INVALID -> {
                                ConnectState.Error.WrongCredentials
                            }
                            CredentialsInput.Reason.FAILURE -> failure.failure()?.let {
                                ConnectState.Error.General(it)
                            } ?: ConnectState.Error.General(
                                failure = failureOf("Login failed")
                            )
                        }
                    }
                    model.awaits(LoginFlowInput.UnlockTokenInput::class.java) -> {
                        // todo handle UnlockTokenInput
                        null
                    }
                    else -> null
                }
                else -> when {
                    // SDK is either attesting or still processing.
                    model.state.attesting || model.state.processing -> {
                        _state.value = ConnectState.Loading
                    }
                    // Final state: Login was successful.
                    model.state.success -> {
                        _state.value = ConnectState.Success
                    }
                    // Final state: Login failed.
                    model.state.failure -> {
                        viewModelScope.launch {
                            val failureUpdate = update as Update.Failure
                            _state.value = ConnectState.Error.General(failureUpdate.failure)
                        }
                    }

                    else -> null
                }
            }
        }
        true
    }

    fun init(loginManager: LoginManager) {
        flow = loginManager.activeFlow ?: run {
            val variant = if (loginManager.locked) {
                LoginFlowVariant.UNLOCK
            } else {
                LoginFlowVariant.LOGIN
            }
            val options = LoginFlowOptions.of(variant = variant)
            loginManager.newFlow(options)
        }

        flow.subscribe(receiver)
    }

    fun login(username: String, password: String) {
        flow.dispatch(
            CredentialsInput(
                username = username.toCharArray(),
                password = password.toCharArray()
            )
        )
    }
}

internal sealed class ConnectState {
    data object Idle: ConnectState()
    data object Loading: ConnectState()
    data object CredentialsInput: ConnectState()

    sealed class Error: ConnectState() {
        data object WrongCredentials: Error()
        data class General(val failure: Failure): Error()
    }
    data object Success: ConnectState()
}