package de.tillhub.paymentengine.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.demo.ui.components.DropDown
import de.tillhub.paymentengine.demo.ui.components.Greeting
import de.tillhub.paymentengine.demo.ui.terminalconfigs.OPIConfig
import de.tillhub.paymentengine.demo.ui.terminalconfigs.SPOSConfig
import de.tillhub.paymentengine.demo.ui.terminalconfigs.ZVTLocalConfig
import de.tillhub.paymentengine.demo.ui.terminalconfigs.ZVTRemoteConfig
import de.tillhub.paymentengine.demo.ui.theme.DemoPaymentTheme
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val paymentEngine: PaymentEngine by lazy {
        PaymentEngine.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        viewModel.initPaymentManager(paymentEngine.newPaymentManager(this))
        viewModel.initRefundManager(paymentEngine.newRefundManager(this))
        viewModel.initReversalManager(paymentEngine.newReversalManager(this))
        viewModel.initReconciliationManager(paymentEngine.newReconciliationManager(this))
        viewModel.initConnectionManager(paymentEngine.newConnectionManager(this))

        paymentEngine.setAnalytics(object : PaymentAnalytics {
            override fun logOperation(request: String) {
                Timber.tag("PaymentAnalytics").d(request)
            }

            override fun logCommunication(protocol: String, message: String) {
                Timber.tag("PaymentAnalytics").d("$protocol\n$message")
            }
        })

        setContent {
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
                        Greeting(viewModel.cardManagerState.collectAsState(TerminalOperationStatus.Waiting))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DropDown(
                                listOf("s-pos", "opi", "zvt-local", "zvt-remote"),
                                "Terminal Type",
                                {
                                    viewModel.terminalID.value = it
                                }
                            )
                        }
                        TerminalConfig(viewModel.terminalID)
                    }
                }
            }
        }
    }
    @Composable
    private fun TerminalConfig(terminalID: MutableStateFlow<String>){
        when(terminalID.collectAsState().value) {
            "s-pos" ->
                SPOSConfig(
                    connectAction = { viewModel.startConnect() },
                    disconnectAction = { viewModel.startSPOSDisconnect() }
                )

            "zvt-remote" ->
                ZVTRemoteConfig(
                    paymentAction = { viewModel.startPayment() },
                    refundAction = { viewModel.startRefund() },
                    reversalAction = { viewModel.startReversal() },
                    reconciliationAction = { viewModel.startReconciliation() },
                    connectAction = { viewModel.startConnect() },
                    viewModel.remoteIP,
                    viewModel.port1
                )

            "zvt-local" ->
                ZVTLocalConfig(
                    paymentAction = { viewModel.startPayment() },
                    refundAction = { viewModel.startRefund() },
                    reversalAction = { viewModel.startReversal() },
                    reconciliationAction = { viewModel.startReconciliation() },
                    connectAction = { viewModel.startConnect() },
                    viewModel.port1,
                )

            else ->
                OPIConfig(
                    paymentAction = { viewModel.startPayment() },
                    refundAction = { viewModel.startRefund() },
                    reversalAction = { viewModel.startReversal() },
                    reconciliationAction = { viewModel.startReconciliation() },
                    connectAction = { viewModel.startConnect() },
                    viewModel.remoteIP,
                    viewModel.port1,
                    viewModel.port2
                )
        }
    }
}
