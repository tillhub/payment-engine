package de.tillhub.paymentengine.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import de.lavego.ISO4217.ISOCurrency
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
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

abstract class CardTerminalActivity : PaymentTerminalActivity() {

    private val viewModel by viewModels<CardTerminalViewModel>()

    @Inject
    lateinit var cardPaymentManager: CardPaymentManager

    private val activityManager: ActivityManager by lazy {
        applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

    fun doPayment(paymentAmount: BigDecimal, currency: ISOAlphaCurrency) {
        showInstructions()
        doPayment(Payment(paymentAmount, currency.value))
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
    fun doPartialRefund(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val partialRefund = Apdu(Commons.Command.CMD_0631).apply {
            val password = cardPaymentManager.getSaleConfiguration().pin
            val currencyAsTwoByteHex = ISOCurrency.byAlpha(currency.value).codeAsTwoByteHex()
            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(Bmp(0x04.toByte(), Commons.NumberToBCD(amount.toLong(), AMOUNT_BYTE_COUNT)))
            add(Bmp(0x49.toByte(), Commons.StringNumberToBCD(currencyAsTwoByteHex, CC_BYTE_COUNT)))
        }

        doCustom(partialRefund.apdu())
    }

    private fun doZVTSetup() {
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

    private fun doSetup() {
        if (cardPaymentManager.getTransportConfiguration().paymentProtocol == PaymentProtocol.Nexo) {
            Toast.makeText(this, "Nexo payment protocol is not supported", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            doZVTSetup()
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
        // Part of Nexo. Not supported yet
    }

    override fun onPaymentResult(transactionData: TransactionData) {
        // Part of Nexo. Not supported yet
    }

    override fun onResponse(response: String, type: Int) {
        // Part of Nexo. Not supported yet
    }

    override fun onStatus(status: String) {
        super.onStatus(status)
        cardPaymentManager.onStatus(status)
    }

    override fun onIntermediateStatus(status: String) {
        super.onIntermediateStatus(status)
        showIntermediateStatus(status)
    }

    override fun onCompletion(completion: String) {
        super.onCompletion(completion)
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        Toast.makeText(applicationContext, "onCompletion", Toast.LENGTH_SHORT).show()
        if (viewModel.terminalOperationState.value == TerminalOperationState.Operation) {
            cardPaymentManager.onCompletion(completion)
        }
        viewModel.onOperationCompleted()
    }

    override fun onReceipt(receipt: String) {
        super.onReceipt(receipt)
        cardPaymentManager.onReceipt(receipt)
    }

    override fun onError(error: String) {
        super.onError(error)
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        Toast.makeText(applicationContext, "onCompletion", Toast.LENGTH_SHORT).show()
        cardPaymentManager.onError(error)
        finish()
    }

    companion object {
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
