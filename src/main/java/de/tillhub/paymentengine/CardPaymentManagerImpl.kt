package de.tillhub.paymentengine

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lavego.sdk.PaymentProtocol
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransportConfiguration
import de.tillhub.paymentengine.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardPaymentManagerImpl(
    private val appContext: Context,
    private val cardPaymentConfigRepository: CardPaymentConfigRepository,
    private val cardSaleConfigRepository: CardSaleConfigRepository,
    private val terminalConfig: TerminalConfig,
    private val lavegoTransactionDataConverter: LavegoTransactionDataConverter
) : CardPaymentManager {

    @Inject
    constructor(
        @ApplicationContext appContext: Context,
        cardPaymentConfigRepository: CardPaymentConfigRepository,
        cardSaleConfigRepository: CardSaleConfigRepository,
        terminalConfig: TerminalConfig
    ) : this(
        appContext,
        cardPaymentConfigRepository,
        cardSaleConfigRepository,
        terminalConfig,
        LavegoTransactionDataConverter()
    )

    @VisibleForTesting
    var activeTerminalConnection: LavegoConnection? = null

    private val _transactionState: MutableStateFlow<LavegoTerminalOperation> =
        MutableStateFlow(LavegoTerminalOperation.Waiting)

    override val transactionStateFlow: StateFlow<LavegoTerminalOperation> = _transactionState
    override val transactionState: LavegoTerminalOperation
        get() = _transactionState.value

    override fun connect(activity: Activity): PaymentTerminalConnection? =
        when (activity) {
            is ComponentActivity -> LavegoConnection(activity, appContext).apply {
                activeTerminalConnection = this
            }
            else -> null
        }

    override fun disconnect(connection: PaymentTerminalConnection) {
        if (activeTerminalConnection == connection) {
            activeTerminalConnection = null
        }
        connection.disconnect()
    }

    private var lastReceipt: LavegoReceiptBuilder? = null
    private var lastData: String? = null

    override fun startPaymentTransaction(amount: BigDecimal, currency: String) {
        _transactionState.value = LavegoTerminalOperation.Pending.Payment(amount, currency)

        activeTerminalConnection?.startPaymentTransaction()
    }

    override fun startReversalTransaction(receiptNo: String) {
        _transactionState.value = LavegoTerminalOperation.Pending.Reversal(receiptNo)

        activeTerminalConnection?.startPaymentReversal()
    }

    override fun startPartialRefundTransaction(amount: BigDecimal) {
        _transactionState.value = LavegoTerminalOperation.Pending.PartialRefund(amount)

        activeTerminalConnection?.startPartialRefund()
    }

    override fun startReconciliation() {
        _transactionState.value = LavegoTerminalOperation.Pending.Reconciliation

        activeTerminalConnection?.startReconciliation()
    }

    override fun clearTransaction() {
        _transactionState.value = LavegoTerminalOperation.Waiting
        lastReceipt = null
        lastData = null
    }

    override fun getTransportConfiguration(): TransportConfiguration =
        TransportConfiguration().apply {
            when (cardPaymentConfigRepository.config.integrationType) {
                IntegrationType.ZVT -> {
                    paymentProtocol = PaymentProtocol.Zvt
                    host = cardPaymentConfigRepository.config.ipAddress
                    port = cardPaymentConfigRepository.config.port
                }
                IntegrationType.NEXO -> {
                    paymentProtocol = PaymentProtocol.Nexo
                    host = CardPaymentConfig.LOCALHOST_IP
                    port = CardPaymentConfig.NEXO_PORT
                }
            }
        }

    override fun getSaleConfiguration(): SaleConfiguration {
        return cardSaleConfigRepository.config.let { config ->
            SaleConfiguration().apply {
                applicationName = config.applicationName
                operatorId = config.operatorId
                saleId = config.saleId
                pin = config.pin
                poiId = config.poiId
                poiSerialnumber = config.poiSerialNumber
                trainingMode = config.trainingMode

                with (zvtFlags) {
                    isoCurrencyRegister(config.zvtConfig.isoCurrencyNumber)
                    paymentType(if (config.zvtConfig.terminalPrinterAvailable) {
                        0b1000100.toByte() // terminal supports the printer ready bit (3)
                    } else {
                        0b1000000.toByte() // terminal does NOT support the printer ready bit (3)
                    })
                }
            }
        }
    }

    override fun onStatus(status: String) {
        lastData = status
    }

    override fun onReceipt(receipt: String) {
        if (lastReceipt == null) {
            lastReceipt = LavegoReceiptBuilder()
        }
        if (receipt.contains('\n')) {
            lastReceipt!!.addBlock(receipt)
        } else {
            lastReceipt!!.addLine(receipt)
        }
    }

    override fun onCompletion(completion: String) {
        terminalConfig.terminalScope.launch {
            _transactionState.value = LavegoTerminalOperation.Success(
                date = terminalConfig.timeNow(),
                customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                rawData = lastData.orEmpty(),
                data = lastData?.let {
                    lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
                },
            )
        }
    }

    override fun onError(error: String) {
        terminalConfig.terminalScope.launch {
            _transactionState.value = LavegoTerminalOperation.Failed(
                date = terminalConfig.timeNow(),
                customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                rawData = lastData.orEmpty(),
                data = lastData?.let {
                    lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
                },
            )
        }
    }
}