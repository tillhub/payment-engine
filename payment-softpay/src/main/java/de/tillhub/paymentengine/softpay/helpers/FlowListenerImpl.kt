package de.tillhub.paymentengine.softpay.helpers

import io.softpay.sdk.Softpay
import io.softpay.sdk.config.ConfigFlow
import io.softpay.sdk.config.ConfigManager
import io.softpay.sdk.domain.Entity
import io.softpay.sdk.flow.Flow
import io.softpay.sdk.flow.FlowAction
import io.softpay.sdk.flow.FlowFactory
import io.softpay.sdk.flow.FlowListener
import io.softpay.sdk.flow.FlowModel
import io.softpay.sdk.flow.FlowOptions
import io.softpay.sdk.flow.FlowRequirement
import io.softpay.sdk.flow.result
import io.softpay.sdk.login.LoginFlow
import io.softpay.sdk.login.LoginManager
import io.softpay.sdk.transaction.TransactionFlow

internal class FlowListenerImpl : FlowListener {

    override fun onFlowStart(softpay: Softpay, flow: Flow<FlowModel, FlowAction>): Boolean {
        if (flow.subscriptionRequired() != FlowRequirement.MANDATORY) {
            return false
        }

        if (flow.foregroundRequired() == FlowRequirement.DISALLOWED) {
            flow.subscribeSilently()
            return true
        }

        return when (flow) {
            is LoginFlow -> startLogin(softpay.loginManager)

            is ConfigFlow -> startConfig(softpay.configManager)

            is TransactionFlow -> startTransaction(flow)

            else -> false
        }
    }

    private fun startLogin(loginManager: LoginManager): Boolean {
        if (loginManager.isAppBusy) {
            return false
        }

        // todo
        return true
    }

    private fun startConfig(configManager: ConfigManager): Boolean {
        if (configManager.isAppBusy) {
            return false
        }

        // todo
        return true
    }

    private fun startTransaction(flow: TransactionFlow): Boolean {
        // todo
        return true
    }

    private fun Flow<FlowModel, FlowAction>.subscribeSilently() {
        subscribe { model ->
            if (model.state.final) {
                model.flow.unsubscribe("Silently subscribed flow has ended: ${model.result}")
            }
            true
        }
    }

    private val <
            O : FlowOptions,
            M : FlowModel,
            A : FlowAction,
            F : Flow<M, A>
        > FlowFactory<O, M, A, F>.isAppBusy: Boolean
        get() = filter(active = false) { it.subscribedTo(Entity.APP) }.isNotEmpty()
}