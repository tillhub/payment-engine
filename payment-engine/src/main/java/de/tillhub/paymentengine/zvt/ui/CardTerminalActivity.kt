package de.tillhub.paymentengine.zvt.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.BundleCompat
import de.lavego.sdk.PaymentProtocol
import de.lavego.sdk.PaymentTerminalActivity
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransactionData
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.api.Bmp
import de.lavego.zvt.api.Commons
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal

@Suppress("TooManyFunctions")
abstract class CardTerminalActivity : PaymentTerminalActivity() {

    private val viewModel by viewModels<CardTerminalViewModel>()

    private val activityManager: ActivityManager by lazy {
        applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    protected val config: Terminal.ZVT by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, Terminal.ZVT::class.java)
        } ?: throw IllegalArgumentException("CardTerminalActivity: Extras is null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.terminalOperationState.observe(this) { state ->
            when (state) {
                CardTerminalViewModel.State.Idle -> showLoader()
                CardTerminalViewModel.State.Setup -> doSetup()
                CardTerminalViewModel.State.Operation -> {
                    showInstructions()
                    startOperation()
                }
                is CardTerminalViewModel.State.Error -> finishWithError(state)
                is CardTerminalViewModel.State.Success -> finishWithSuccess(state)
            }
        }

        bindService()
    }

    override fun onStart() {
        super.onStart()
        viewModel.init()
    }

    private fun doZVTSetup() {
        val register = Apdu(Commons.Command.CMD_0600).apply {
            val password = config.saleConfig.pin
            val currency = (config as? Terminal.ZVT)?.isoCurrencyNumber
                ?: throw IllegalArgumentException("Terminal currency is missing")

            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(TERMINAL_CONFIG_BYTE)
            add(Commons.StringNumberToBCD(currency, CC_BYTE_COUNT))
            add(Bmp(REGISTER_TLV_CONTAINER_STRING))
        }

        doCustom(register.apdu())
    }

    private fun doSetup() {
        doZVTSetup()
    }

    abstract fun showLoader()
    abstract fun showInstructions()
    abstract fun showIntermediateStatus(status: String)
    abstract fun startOperation()

    override fun transportConfiguration(): TransportConfiguration {
        return TransportConfiguration().apply {
            paymentProtocol = PaymentProtocol.Zvt
            host = config.ipAddress
            port = config.port
        }
    }

    override fun saleConfiguration(): SaleConfiguration {
        return SaleConfiguration().apply {
            applicationName = config.saleConfig.applicationName
            operatorId = config.saleConfig.operatorId
            saleId = config.saleConfig.saleId
            pin = config.saleConfig.pin
            poiId = config.saleConfig.poiId
            poiSerialnumber = config.saleConfig.poiSerialNumber

            with(zvtFlags) {
                isoCurrencyRegister(config.isoCurrencyNumber)
                paymentType(
                    if (config.terminalPrinterAvailable) {
                        0b1000100.toByte() // terminal supports the printer ready bit (3)
                    } else {
                        0b1000000.toByte() // terminal does NOT support the printer ready bit (3)
                    }
                )
            }
        }
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

    override fun onCompletion(completion: String) {
        super.onCompletion(completion)
        viewModel.onCompletion()
    }

    override fun onError(error: String) {
        super.onError(error)
        viewModel.onError()
    }

    private fun finishWithSuccess(state: CardTerminalViewModel.State.Success) {
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.toTerminalOperation()) }
        )
        finish()
    }

    private fun finishWithError(state: CardTerminalViewModel.State.Error) {
        activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, state.toTerminalOperation()) }
        )
        finish()
    }

    companion object {
        const val PASSWORD_BYTE_COUNT: Int = 3
        const val CC_BYTE_COUNT: Int = 2

        private const val REGISTER_TLV_CONTAINER_STRING = "06 0C 12 01 30 27 03 14 01 FE 40 02 B0 B0"
        private const val TERMINAL_CONFIG_BYTE: Byte = 0b11000110.toByte()
    }
}