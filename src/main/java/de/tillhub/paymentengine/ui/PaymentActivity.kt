package de.tillhub.paymentengine.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.lavego.sdk.*
import de.tillhub.paymentengine.CardPaymentManager.Companion.EXTRA_CARD_PAYMENT_CONFIG
import de.tillhub.paymentengine.CardPaymentManager.Companion.EXTRA_CARD_SALE_CONFIG
import de.tillhub.paymentengine.SetupProtocol
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

abstract class PaymentActivity : PaymentTerminalActivity() {

    private val viewModel by viewModels<PaymentViewModel>()

    private lateinit var cardPaymentConfig: CardPaymentConfig
    private lateinit var cardSaleConfig: CardSaleConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardPaymentConfig = intent.getParcelableExtra(EXTRA_CARD_PAYMENT_CONFIG)
            ?: throw IllegalArgumentException("Missing argument: $EXTRA_CARD_PAYMENT_CONFIG")
        cardSaleConfig = intent.getParcelableExtra(EXTRA_CARD_SALE_CONFIG)
            ?: throw IllegalArgumentException("Missing argument: $EXTRA_CARD_SALE_CONFIG")

        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            delay(SERVICE_START_DELAY)
            viewModel.init(cardPaymentConfig, cardSaleConfig)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionState.collect { state ->
                    when(state) {
                        PaymentState.Idle -> Unit
                        is PaymentState.Setup -> {
                            when(state.protocol) {
                                SetupProtocol.Nexo -> doNexoLogin()
                                is SetupProtocol.ZVT -> doZVTSetup(state.protocol)
                            }
                        }
                        PaymentState.Ready -> TODO()
                        is PaymentState.Success -> {
                            setResult(RESULT_OK, Intent())
                        }
                    }
                }
            }
        }
    }

    private fun doNexoLogin() {
        Toast.makeText(this, "Nexo protocol is not supported.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun doZVTSetup(protocol: SetupProtocol.ZVT) {
        doCustom(protocol.register.apdu())
    }

    // Lavego
    override fun transportConfiguration(): TransportConfiguration {
        return viewModel.transportConfiguration(cardPaymentConfig)
    }

    override fun saleConfiguration(): SaleConfiguration {
        return viewModel.saleConfiguration(cardSaleConfig)
    }

    override fun launchSelf(delayMillies: Int) {
        lifecycleScope.launch {
            delay(delayMillies.toLong())

            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        }
    }

    override fun onPaymentResult(result: TransactionData) {
        // Part of Nexo. Not supported yet
    }

    override fun onResponse(response: String, type: Int) {
        // Part of Nexo. Not supported yet
    }

    // ZVT
    override fun onStatus(status: String) {
        super.onStatus(status)
        viewModel.onStatus(status)
    }

    override fun onIntermediateStatus(status: String) {
        super.onIntermediateStatus(status)
        showIntermediateStatus(status)
    }

    override fun onReceipt(receipt: String) {
        super.onReceipt(receipt)
        viewModel.onReceipt(receipt)
    }

    override fun onError(error: String) {
        super.onError(error)
        viewModel.onError(error)
    }

    override fun onCompletion(completion: String) {
        super.onCompletion(completion)
        viewModel.onCompletion(completion)
    }

    abstract fun showIntermediateStatus(status: String)

    companion object {
        private const val SERVICE_START_DELAY: Long = 500
    }
}