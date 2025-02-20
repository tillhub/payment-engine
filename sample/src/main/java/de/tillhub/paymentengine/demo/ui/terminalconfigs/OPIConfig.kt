package de.tillhub.paymentengine.demo.ui.terminalconfigs

import androidx.compose.runtime.Composable
import de.tillhub.paymentengine.demo.storage.TerminalData
import de.tillhub.paymentengine.demo.ui.components.ActionButton
import de.tillhub.paymentengine.demo.ui.components.SimpleTextField

@Composable
fun OPIConfig(
    paymentAction: () -> Unit,
    refundAction: () -> Unit,
    reversalAction: () -> Unit,
    reconciliationAction: () -> Unit,
    connectAction: () -> Unit,
    terminalData: TerminalData,
    ipChange: (String) -> Unit,
    port1Change: (String) -> Unit,
    port2Change: (String) -> Unit,
) {
    SimpleTextField("IP Address", terminalData.ipAddress) {
        ipChange(it)
    }
    SimpleTextField("Network Port", terminalData.port1) {
        port1Change(it)
    }
    SimpleTextField("Network Port 2", terminalData.port2) {
        port2Change(it)
    }
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
