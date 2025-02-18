package de.tillhub.paymentengine.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.demo.di.ViewModelFactory
import de.tillhub.paymentengine.demo.di.SampleAppApplication
import de.tillhub.paymentengine.demo.ui.components.Greeting
import de.tillhub.paymentengine.demo.ui.theme.DemoPaymentTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private val paymentEngine: PaymentEngine by lazy {
        PaymentEngine.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val deviceRepository = (applicationContext as SampleAppApplication).deviceRepository
        val factory = ViewModelFactory(deviceRepository)

        val viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

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
                    ) {
                        Greeting(viewModel.cardManagerState.collectAsState(TerminalOperationStatus.Waiting))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TerminalSettings()
                        }
                    }
                }
            }
        }
    }
}
