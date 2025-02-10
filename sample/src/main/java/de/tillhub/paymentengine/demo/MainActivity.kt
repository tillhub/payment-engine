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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.demo.ui.theme.DemoPaymentTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val paymentEngine: PaymentEngine by lazy {
        PaymentEngine.getInstance()
    }
    private val selectedTerminal = "s-pos"


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
                                viewModel
                            )
                            IPAddressTextField(
                                viewModel
                            )
                        }
                        ActionButton("Payment") {
                            viewModel.startPayment()
                        }
                        ActionButton("Refund") {
                            viewModel.startRefund()
                        }
                        ActionButton("Reversal") {
                            viewModel.startReversal()
                        }
                        ActionButton("Reconciliation") {
                            viewModel.startReconciliation()
                        }

                        ActionButton("Connect/Login") {
                            viewModel.startSPOSConnect()
                        }
                        ActionButton("S-POS disconnect") {
                            viewModel.startSPOSDisconnect()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IPAddressTextField(viewModel: MainViewModel) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    TextField(
        value = text,
        onValueChange = { newText: TextFieldValue ->
            text = newText
            viewModel.remoteIP = newText.text
        },
        label = { Text("Remote IP Address") },
        modifier = Modifier.padding(vertical = 3.dp)

    )
}

@Composable
fun Greeting(state: State<TerminalOperationStatus>, modifier: Modifier = Modifier) {
    Text(
        text = state.value.toString(),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(listOfItems: List<String>, viewModel: MainViewModel) {
    var selectedItem by remember { mutableStateOf(listOfItems[0]) }
    var isExpanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        Modifier.padding(vertical = 3.dp)
        ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text("Terminal Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            listOfItems.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        selectedItem = listOfItems[index]
                        isExpanded = false
                        viewModel.terminalID = listOfItems[index]
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun ActionButton(name: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = name
        )
    }
}
