package de.tillhub.paymentengine.demo.ui.terminalconfigs

import androidx.compose.runtime.Composable
import de.tillhub.paymentengine.demo.ui.components.ActionButton

@Composable
fun SPOSConfig(
    connectAction: () -> Unit,
    disconnectAction: () -> Unit
) {
    ActionButton("Connect/Login") {
        connectAction()
    }
    ActionButton("S-POS disconnect") {
        disconnectAction()
    }
}
