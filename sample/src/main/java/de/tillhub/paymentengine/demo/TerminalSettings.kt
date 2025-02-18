package de.tillhub.paymentengine.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tillhub.paymentengine.demo.ui.components.DropDown
import de.tillhub.paymentengine.demo.ui.terminalconfigs.OPIConfig
import de.tillhub.paymentengine.demo.ui.terminalconfigs.SPOSConfig
import de.tillhub.paymentengine.demo.ui.terminalconfigs.ZVTConfig
import de.tillhub.paymentengine.demo.ui.theme.DemoPaymentTheme

@Composable
fun TerminalSettings (
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
) {
    val terminalData by viewModel.terminalData.collectAsState()

    DemoPaymentTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DropDown(
                        listOf("s-pos", "opi", "zvt"),
                        "Terminal Type",
                        {
                            if (it == "zvt") viewModel.terminalID.value = "zvt-remote"
                            else viewModel.terminalID.value = it
                        }
                    )
                }
                when (viewModel.terminalID.collectAsState().value) {
                    "s-pos" ->
                        SPOSConfig(
                            connectAction = { viewModel.startConnect() },
                            disconnectAction = { viewModel.startSPOSDisconnect() }
                        )

                    "zvt-remote" ->
                        ZVTConfig(
                            paymentAction = { viewModel.startPayment() },
                            refundAction = { viewModel.startRefund() },
                            reversalAction = { viewModel.startReversal() },
                            reconciliationAction = { viewModel.startReconciliation() },
                            connectAction = { viewModel.startConnect() },
                            terminalData,
                            ipChange = { viewModel.updateIpAddress(it) },
                            port1Change = { viewModel.updatePort1(it) },
                        )

                    else ->
                        OPIConfig(
                            paymentAction = { viewModel.startPayment() },
                            refundAction = { viewModel.startRefund() },
                            reversalAction = { viewModel.startReversal() },
                            reconciliationAction = { viewModel.startReconciliation() },
                            connectAction = { viewModel.startConnect() },
                            terminalData,
                            ipChange = { viewModel.updateIpAddress(it) },
                            port1Change = { viewModel.updatePort1(it) },
                            port2Change = { viewModel.updatePort2(it) },
                        )
                }
            }
        }
    }
}
