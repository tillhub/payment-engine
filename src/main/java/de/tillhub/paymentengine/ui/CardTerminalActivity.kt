package de.tillhub.paymentengine.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.EntryPointAccessors
import de.lavego.sdk.Constants
import de.lavego.sdk.Payment
import de.lavego.sdk.PaymentProtocol
import de.lavego.sdk.PaymentTerminalActivity
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransactionData
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.api.Bmp
import de.lavego.zvt.api.Commons
import de.tillhub.paymentengine.CardPaymentManager
import de.tillhub.paymentengine.di.DaggerPaymentComponent
import de.tillhub.paymentengine.di.PaymentModuleDependencies
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

abstract class CardTerminalActivity : PaymentTerminalActivity() {

    private val viewModel by viewModels<CardTerminalViewModel>()

    @Inject
    lateinit var cardPaymentManager: CardPaymentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerPaymentComponent.builder()
            .context(this)
            .appDependencies(
                EntryPointAccessors.fromApplication(
                    applicationContext, PaymentModuleDependencies::class.java
                )
            )
            .build()
            .inject(this)
        super.onCreate(savedInstanceState)

        viewModel.terminalOperationState.observe(this) { state ->
            when (state) {
                TerminalOperationState.Idle -> Unit
                TerminalOperationState.Setup -> doSetup()
                TerminalOperationState.Operation -> startOperation()
                TerminalOperationState.Done -> finish()
            }
        }

        bindService()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            delay(SERVICE_START_DELAY)
            viewModel.init()
        }
    }

    fun doPayment(paymentAmount: BigDecimal) {
        when (cardPaymentManager.getTransportConfiguration().paymentProtocol) {
            PaymentProtocol.Nexo -> {
                doPayment(Payment().apply {
                    amount = paymentAmount.divide(BigDecimal.valueOf(100))
                })
            }
            else -> {
                showInstructions()
                doPayment(Payment().apply {
                    amount = paymentAmount
                })
            }
        }
    }

    /**
     * This method is called to initiate a card transaction reversal
     *
     * @param receiptNo the receipt number of the transaction we want to reverse
     */
    fun doCancellation(receiptNo: String) {
        val cancellation = Apdu(Commons.Command.CMD_0630).apply {
            val password = cardPaymentManager.getSaleConfiguration().pin
            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(Bmp(0x87.toByte(), Commons.NumberToBCD(receiptNo.toLong(), RECEIPT_NO_BYTE_COUNT)))
        }

        doCustom(cancellation.apdu())
    }

    /**
     * This method is called to initiate a card transaction partial refund
     *
     * @param amount the amount to be refunded
     */
    fun doPartialRefund(amount: BigDecimal) {
        val partialRefund = Apdu(Commons.Command.CMD_0631).apply {
            val password = cardPaymentManager.getSaleConfiguration().pin
            val currency = cardPaymentManager.getSaleConfiguration().zvtFlags.isoCurrencyRegister()
            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(Bmp(0x04.toByte(), Commons.NumberToBCD(amount.toLong(), AMOUNT_BYTE_COUNT)))
            add(Bmp(0x49.toByte(), Commons.StringNumberToBCD(currency, CC_BYTE_COUNT)))
        }

        doCustom(partialRefund.apdu())
    }

    private fun doZVTRegister() {
        val register = Apdu(Commons.Command.CMD_0600).apply {
            val password = cardPaymentManager.getSaleConfiguration().pin
            val currency = cardPaymentManager.getSaleConfiguration().zvtFlags.isoCurrencyRegister()

            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(TERMINAL_CONFIG_BYTE)
            add(Commons.StringNumberToBCD(currency, CC_BYTE_COUNT))
            add(Bmp(REGISTER_TLV_CONTAINER_STRING))
        }

        doCustom(register.apdu())
    }

    private fun doNexoLogin() {
        showLoader()
        command(Constants.LOGIN)
    }

    private fun doSetup() {
        if (cardPaymentManager.getTransportConfiguration().paymentProtocol == PaymentProtocol.Nexo) {
            doNexoLogin()
        } else {
            doZVTRegister()
        }
    }

    abstract fun showLoader()
    abstract fun showInstructions()
    abstract fun showIntermediateStatus(status: String)
    abstract fun startOperation()

    override fun transportConfiguration(): TransportConfiguration {
        return cardPaymentManager.getTransportConfiguration()
    }

    override fun saleConfiguration(): SaleConfiguration {
        return cardPaymentManager.getSaleConfiguration()
    }

    override fun launchSelf(delay: Int) {
        lifecycleScope.launch {
            delay(delay.toLong())

            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        }
    }

    override fun onPaymentResult(transactionData: TransactionData) {
        cardPaymentManager.setNexoResponse(transactionData)
        showLoader()

        command(Constants.LOGOUT)
    }

    override fun onResponse(response: String, type: Int) {
        if (type == Constants.LOGIN) {
            cardPaymentManager.nexoLoggedIn()
            viewModel.onOperationCompleted()
        } else if (type == Constants.LOGOUT) {
            launchSelf(SELF_LAUNCH_DELAY)
        }
    }

    override fun onRawData(hex: String) {
        super.onRawData(hex)
        cardPaymentManager.getZvtResponseCallback().onRawData(hex)
    }

    override fun onStatus(status: String) {
        super.onStatus(status)
        cardPaymentManager.getZvtResponseCallback().onStatus(status)
    }

    override fun onIntermediateStatus(status: String) {
        super.onIntermediateStatus(status)
        cardPaymentManager.getZvtResponseCallback().onIntermediateStatus(status)
        showIntermediateStatus(status)
    }

    override fun onCompletion(completion: String) {
        super.onCompletion(completion)
        if (viewModel.terminalOperationState.value == TerminalOperationState.Operation) {
            cardPaymentManager.getZvtResponseCallback().onCompletion(completion)
        }

        viewModel.onOperationCompleted()
    }

    override fun onReceipt(receipt: String) {
        super.onReceipt(receipt)
        cardPaymentManager.getZvtResponseCallback().onReceipt(receipt)
    }

    override fun onError(error: String) {
        super.onError(error)
        cardPaymentManager.getZvtResponseCallback().onError(error)

        finish()
    }

    override fun onSocketConnected(connected: Boolean) {
        super.onSocketConnected(connected)
        cardPaymentManager.getZvtResponseCallback().onSocketConnected(connected)
    }

    companion object {
        private const val SELF_LAUNCH_DELAY: Int = 350
        private const val PASSWORD_BYTE_COUNT: Int = 3
        private const val RECEIPT_NO_BYTE_COUNT: Int = 2
        private const val AMOUNT_BYTE_COUNT: Int = 6
        private const val CC_BYTE_COUNT: Int = 2

        private const val REGISTER_TLV_CONTAINER_STRING = "06 0C 12 01 30 27 03 14 01 FE 40 02 B0 B0"
        private const val TERMINAL_CONFIG_BYTE: Byte = 0b11000110.toByte()

        private const val SERVICE_START_DELAY: Long = 500
    }
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }
