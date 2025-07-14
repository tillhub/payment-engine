package de.tillhub.paymentengine.zvt.ui

import android.app.ActivityManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
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
import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.zvt.data.Manufacturer
import de.tillhub.paymentengine.zvt.helper.TimeoutManager
import de.tillhub.paymentengine.zvt.data.ZvtTerminal

@Suppress("TooManyFunctions")
internal abstract class CardTerminalActivity : PaymentTerminalActivity() {

    protected val viewModel by viewModels<CardTerminalViewModel>()

    private val activityManager: ActivityManager by lazy {
        applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    protected val config: ZvtTerminal by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, ZvtTerminal::class.java)
        } ?: throw IllegalArgumentException("CardTerminalActivity: Extras is null")
    }

    protected val analytics: PaymentAnalytics? by lazy {
        PaymentEngine.getInstance().paymentAnalytics
    }

    private lateinit var timeoutManager: TimeoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.terminalOperationState.observe(this) { state ->
            when (state) {
                CardTerminalViewModel.State.Idle -> {
                    timeoutManager.processStarted()
                    showLoader()
                }
                CardTerminalViewModel.State.Setup -> doZVTSetup()
                CardTerminalViewModel.State.Operation -> {
                    showInstructions()
                    startOperation()
                }
                is CardTerminalViewModel.State.Error -> {
                    timeoutManager.processFinishedWithResult()
                    finishWithError(state)
                }
                is CardTerminalViewModel.State.Success -> {
                    timeoutManager.processFinishedWithResult()
                    finishWithSuccess(state)
                }
                CardTerminalViewModel.State.OperationAborted -> finish()
            }
        }

        timeoutManager = TimeoutManager(this) {
            setCancelVisibility(true)
        }

        onBackPressedDispatcher.addCallback(
            owner = this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            }
        )

        bindService()
    }

    override fun onStart() {
        super.onStart()
        viewModel.init()
    }

    private fun doZVTSetup() {
        analytics?.logOperation("Operation: LOGIN\n$config")

        val register = Apdu(Commons.Command.CMD_0600).apply {
            val password = config.saleConfig.pin
            val currency = config.isoCurrencyNumber

            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(TERMINAL_CONFIG_BYTE)
            add(Commons.StringNumberToBCD(currency, CC_BYTE_COUNT))
            add(Bmp(REGISTER_TLV_CONTAINER_STRING))
        }

        doCustom(register.apdu())
    }

    abstract fun showLoader()
    abstract fun showInstructions()
    abstract fun showIntermediateStatus(status: String)
    abstract fun startOperation()
    abstract fun setCancelVisibility(visible: Boolean)
    abstract fun finishWithError(state: CardTerminalViewModel.State.Error)
    abstract fun finishWithSuccess(state: CardTerminalViewModel.State.Success)

    // Part of Nexo. Not supported yet
    override fun launchSelf(delay: Int) = Unit
    override fun onPaymentResult(transactionData: TransactionData) = Unit
    override fun onResponse(response: String, type: Int) = Unit
    // End of Nexo

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

    override fun onStatus(status: String) {
        super.onStatus(status)
        timeoutManager.processUpdated()

        analytics?.logCommunication(
            protocol = "ZVT",
            message = "RECEIVED:\n$status"
        )

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

        viewModel.onCompletion(::moveAppToFront)
    }

    override fun onError(error: String) {
        super.onError(error)
        viewModel.onError(::moveAppToFront)
    }

    protected fun moveAppToFront() {
        // Only move the app to the front if the manufacturer is not PAX
        if (!Manufacturer.matches(Manufacturer.PAX)) {
            activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        }
    }

    /**
     * This method is called to abort a pending card transaction
     */
    protected fun doAbortOperation() {
        viewModel.abortOperation {
            showLoader()
            setCancelVisibility(false)

            val abort = Apdu(Commons.Command.CMD_06B0).apply {
                add(Bmp(0xD2.toByte(), byteArrayOf(0.toByte())))
                add(Bmp(0xFA.toByte(), byteArrayOf(0xFF.toByte())))
            }
            doCustom(abort.apdu())

            // On certain ZVT terminals the abort command (06 B0) does nothing.
            // For now we will leave the implementation as is, but if it happens to be
            // blocking the app, the implementation will change
        }
    }

    companion object {
        const val PASSWORD_BYTE_COUNT: Int = 3
        const val CC_BYTE_COUNT: Int = 2

        // TLV is static, it represents config so the receipt is coded into the correct character set
        private const val REGISTER_TLV_CONTAINER_STRING = "06 0C 12 01 30 27 03 14 01 FE 40 02 B0 B0"
        private const val TERMINAL_CONFIG_BYTE: Byte = 0b11000110.toByte()
    }
}