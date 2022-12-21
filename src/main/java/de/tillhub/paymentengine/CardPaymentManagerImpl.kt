package de.tillhub.paymentengine

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import de.lavego.sdk.PaymentProtocol
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransactionData
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.ZvtResponseCallback
import de.tillhub.paymentengine.data.*
import de.tillhub.paymentengine.providers.PaymentTime
import de.tillhub.paymentengine.ui.CardPaymentActivity
import de.tillhub.paymentengine.ui.CardPaymentPartialRefundActivity
import de.tillhub.paymentengine.ui.CardPaymentReversalActivity
import de.tillhub.paymentengine.ui.TerminalReconciliationActivity
import kotlinx.coroutines.CoroutineScope
import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CardPaymentManagerImpl(
    private val appContext: Context,
    private val lavegoTransactionDataConverter: LavegoTransactionDataConverter,
    private val paymentTime: PaymentTime,
    private val cardPaymentConfig: CardPaymentConfig,
    private val cardSaleConfig: CardSaleConfig,
    private val applicationScope: CoroutineScope
) : CardPaymentManager {

    @VisibleForTesting
    var activeTerminalConnection: LavegoConnection? = null

    private val _transactionState: MutableStateFlow<LavegoTerminalOperation> =
        MutableStateFlow(LavegoTerminalOperation.Waiting)

    override val transactionStateFlow: StateFlow<LavegoTerminalOperation> = _transactionState
    override val transactionState: LavegoTerminalOperation
        get() = _transactionState.value

    private val _zvtConnectionState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val zvtConnectionStateFlow: StateFlow<Boolean> = _zvtConnectionState
    override val zvtConnectionState: Boolean
        get() = _zvtConnectionState.value

    private var _isNexoLoggedIn: Boolean = false
    override val isNexoLoggedIn: Boolean
        get() = _isNexoLoggedIn

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

    private val zvtResponseCallback: ZvtResponseCallback = object : ZvtResponseCallback {
        override fun onRawData(hex: String) = Unit // no need to handle this

        override fun onStatus(status: String) {
            lastData = status
        }

        override fun onIntermediateStatus(status: String) = Unit // no need to handle this here

        override fun onCompletion(completion: String) {
            applicationScope.launch {
                _transactionState.value = LavegoTerminalOperation.Success(
                    date = paymentTime.now(),
                    customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                    merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                    rawData = lastData.orEmpty(),
                    data = lastData?.let {
                        lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
                    },
                )
            }
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

        override fun onError(error: String) {
            applicationScope.launch {
                _transactionState.value = LavegoTerminalOperation.Failed(
                    date = paymentTime.now(),
                    customerReceipt = lastReceipt?.customerReceipt.orEmpty(),
                    merchantReceipt = lastReceipt?.merchantReceipt.orEmpty(),
                    rawData = lastData.orEmpty(),
                    data = lastData?.let {
                        lavegoTransactionDataConverter.convertFromJson(it).getOrNull()
                    },
                )
            }
        }

        override fun onSocketConnected(connected: Boolean) {
            _zvtConnectionState.value = connected
        }
    }

    override fun startPaymentTransaction(amount: BigDecimal) {
        _transactionState.value = LavegoTerminalOperation.Pending.Payment(amount)

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

    override fun nexoLoggedIn() {
        _isNexoLoggedIn = true
    }

    override fun setNexoResponse(txData: TransactionData) {
        _transactionState.value = LavegoTerminalOperation.Success(
            date = paymentTime.now(),
            customerReceipt = "",
            merchantReceipt = "",
            rawData = "",
            data = LavegoTransactionData(
                additionalText = txData.additional_text,
                aid = txData.aid,
                amount = BigInteger.valueOf(txData.amount),
                cardName = txData.card_name,
                cardType = txData.card_type,
                cardSeqNumber = txData.card_seq_no.toInt(),
                chipData = txData.chip_data,
                data = txData.data,
                date = txData.date,
                expiry = txData.expiry,
                receiptNo = txData.receipt_no.toInt(),
                resultCode = txData.resultCode,
                resultText = txData.resultText,
                singleAmounts = txData.single_amounts,
                tags = txData.tags,
                tid = txData.tid,
                time = txData.time,
                trace = txData.trace,
                traceOrig = txData.trace_orig,
                track1 = txData.track1,
                track2 = txData.track2,
                track3 = txData.track3,
                vu = txData.vu,
            ),
        )
    }

    override fun getZvtResponseCallback(): ZvtResponseCallback = zvtResponseCallback

    @Suppress("MagicNumber")
    override fun getTransportConfiguration(): TransportConfiguration =
        TransportConfiguration().apply {
            when (cardPaymentConfig.integrationType) {
                IntegrationType.ZVT -> {
                    paymentProtocol = PaymentProtocol.Zvt
                    host = cardPaymentConfig.ipAddress
                    port = cardPaymentConfig.port
                }
                IntegrationType.NEXO -> {
                    paymentProtocol = PaymentProtocol.Nexo
                    host = CardPaymentConfig.LOCALHOST_IP
                    port = CardPaymentConfig.NEXO_PORT
                }
            }
        }

    override fun getSaleConfiguration(): SaleConfiguration {
        return SaleConfiguration().apply {
            applicationName = cardSaleConfig.applicationName
            operatorId = cardSaleConfig.operatorId
            saleId = cardSaleConfig.saleId
            pin = cardSaleConfig.pin
            poiId = cardSaleConfig.poiId
            poiSerialnumber = cardSaleConfig.poiSerialNumber
            trainingMode = cardSaleConfig.trainingMode

            zvtFlags.isoCurrencyRegister(cardSaleConfig.isoCurrencyNumber)
        }
    }
}

class LavegoConnection(
    private var componentActivity: ComponentActivity?,
    private val appContext: Context,
) : PaymentTerminalConnection() {

    override fun disconnect() {
        componentActivity = null
    }

    fun startPaymentTransaction() {
        componentActivity?.startActivity(paymentIntent(appContext))
    }

    fun startPaymentReversal() {
        componentActivity?.startActivity(reversalIntent(appContext))
    }

    fun startPartialRefund() {
        componentActivity?.startActivity(partialRefundIntent(appContext))
    }

    fun startReconciliation() {
        componentActivity?.startActivity(reconciliationIntent(appContext))
    }

    companion object {
        private fun paymentIntent(context: Context): Intent =
            Intent(context, CardPaymentActivity::class.java)

        private fun reversalIntent(context: Context): Intent =
            Intent(context, CardPaymentReversalActivity::class.java)

        private fun partialRefundIntent(context: Context): Intent =
            Intent(context, CardPaymentPartialRefundActivity::class.java)

        private fun reconciliationIntent(context: Context): Intent =
            Intent(context, TerminalReconciliationActivity::class.java)
    }
}
