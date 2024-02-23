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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.demo.ui.theme.DemoPaymentTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val paymentEngine: PaymentEngine by lazy {
        PaymentEngine.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initPaymentManager(paymentEngine.newPaymentManager().build(lifecycle))
        viewModel.initRefundManager(paymentEngine.newRefundManager().build(lifecycle))
        viewModel.initReversalManager(paymentEngine.newReversalManager().build(lifecycle))
        viewModel.initReconciliationManager(paymentEngine.newReconciliationManager().build(lifecycle))

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
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(state: State<TerminalOperationStatus>, modifier: Modifier = Modifier) {
    Text(
        text = state.value.toString(),
        modifier = modifier
    )
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
