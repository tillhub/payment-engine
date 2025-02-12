package de.tillhub.paymentengine.demo.ui.terminalconfigs

import androidx.compose.runtime.Composable
import de.tillhub.paymentengine.demo.ui.components.ActionButton
import de.tillhub.paymentengine.demo.ui.components.SimpleTextField
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun OPIConfig(
    paymentAction: () -> Unit,
    refundAction: () -> Unit,
    reversalAction: () -> Unit,
    reconciliationAction: () -> Unit,
    connectAction: () -> Unit,
    ipAddress: MutableStateFlow<String>,
    port1: MutableStateFlow<String>,
    port2: MutableStateFlow<String>
) {
    SimpleTextField("IP Address", ipAddress)
    SimpleTextField("Network Port", port1)
    SimpleTextField("Network Port 2", port2)
    ActionButton("Payment") {
        paymentAction()
    }
    ActionButton("Refund") {
        refundAction()
    }
    ActionButton("Reversal") {
       reversalAction()
    }
    ActionButton("Reconciliation") {
        reconciliationAction()
    }
    ActionButton("Connect/Login") {
        connectAction()
    }
}
