package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.data.TransactionData
import de.tillhub.paymentengine.helper.ResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSResultState
import de.tillhub.paymentengine.spos.data.SPOSTransactionResult
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
internal object SPOSResponseHandler {

    private const val SPOS_PROTOCOL = "SPOS"
    private val RECOVERABLE_ERRORS = listOf("TERMINAL_CONNECTION_LOST", "RESPONSE_TIMEOUT")

    fun handleTerminalConnectResponse(
        resultCode: Int,
        intent: Intent?,
    ): TerminalOperationStatus.Login = if (resultCode == Activity.RESULT_OK) {
        TerminalOperationStatus.Login.Connected(
            date = Instant.now(),
            rawData = "",
            terminalType = Terminal.SPOS.TYPE,
            terminalId = Terminal.SPOS.TYPE,
        )
    } else {
        val error = intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)
            ?: intent?.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)

        error?.let {
            TerminalOperationStatus.Login.Error(
                date = Instant.now(),
                rawData = "",
                resultCode = ResultCodeSets.getSPOSCode(it),
            )
        } ?: TerminalOperationStatus.Login.Canceled
    }

    fun handleTerminalDisconnectResponse(
        resultCode: Int,
    ): TerminalOperationStatus.Login =
        if (resultCode == Activity.RESULT_OK) {
            TerminalOperationStatus.Login.Disconnected(
                date = Instant.now()
            )
        } else {
            TerminalOperationStatus.Login.Canceled
        }

    fun handleTicketReprintResponse(
        resultCode: Int,
        intent: Intent?,
        converter: StringToReceiptDtoConverter = StringToReceiptDtoConverter()
    ): TerminalOperationStatus.TicketReprint =
        if (resultCode == Activity.RESULT_OK) {
            val merchantReceipt =
                intent?.extras?.getReceipt(SPOSKey.ResultExtra.RECEIPT_MERCHANT, converter)
            val customerReceipt =
                intent?.extras?.getReceipt(SPOSKey.ResultExtra.RECEIPT_CUSTOMER, converter)

            val error = intent?.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)

            if (error == null) {
                TerminalOperationStatus.TicketReprint.Success(
                    date = Instant.now(),
                    customerReceipt = customerReceipt.orEmpty(),
                    merchantReceipt = merchantReceipt.orEmpty(),
                    rawData = intent?.extras?.toRawData().orEmpty()
                )
            } else {
                TerminalOperationStatus.TicketReprint.Error(
                    date = Instant.now(),
                    rawData = intent.extras?.toRawData().orEmpty(),
                    resultCode = ResultCodeSets.getSPOSCode(error)
                )
            }
        } else {
            TerminalOperationStatus.TicketReprint.Canceled
        }

    fun <T : TerminalOperationStatus>handleTransactionResult(
        resultCode: Int,
        intent: Intent?,
        analytics: PaymentAnalytics?,
        kClass: KClass<T>,
        converter: StringToReceiptDtoConverter = StringToReceiptDtoConverter()
    ): TerminalOperationStatus {
        val merchantReceipt =
            intent?.extras?.getReceipt(SPOSKey.ResultExtra.RECEIPT_MERCHANT, converter)
        val customerReceipt =
            intent?.extras?.getReceipt(SPOSKey.ResultExtra.RECEIPT_CUSTOMER, converter)
        val resultState = SPOSResultState.find(
            type = intent?.extras?.getString(SPOSKey.ResultExtra.RESULT_STATE).orEmpty()
        )
        val transactionResult = SPOSTransactionResult.find(
            type = intent?.extras?.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT).orEmpty()
        )
        val error = intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)
            ?: intent?.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)

        val operationStatus = if (resultCode == Activity.RESULT_OK) {
            if (transactionResult == SPOSTransactionResult.ACCEPTED && resultState.isSuccessful()) {
                ResponseHandler.wrapSuccess(
                    createSuccess(intent, customerReceipt, merchantReceipt), kClass
                )
            } else {
                ResponseHandler.wrapError(
                    createError(intent, customerReceipt, merchantReceipt, error), kClass
                )
            }
        } else {
            error?.let {
                ResponseHandler.wrapError(
                    createError(intent, customerReceipt, merchantReceipt, error), kClass
                )
            } ?: ResponseHandler.getCanceledStatus(kClass)
        }

        return operationStatus.also {
            if (ResponseHandler.isSuccess(it)) {
                analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.createResultOk(intent?.extras)
                )
            } else {
                analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.createResultCanceled(intent?.extras)
                )
            }
        }
    }

    internal fun Bundle.toRawData(): String {
        val builder = StringBuilder()
        builder.appendLine("Extras {")

        keySet().forEach {
            builder.appendLine("$it = ${getString(it)}")
        }

        builder.append("}")
        return builder.toString()
    }

    fun canResolveTransactionResult(intent: Intent?): Boolean =
        intent?.extras?.containsKey(SPOSKey.ResultExtra.TRANSACTION_RESULT) == true ||
                intent?.extras?.containsKey(SPOSKey.ResultExtra.ERROR) == true

    private fun createSuccess(intent: Intent?, customerReceipt: String?, merchantReceipt: String?) =
        TerminalOperationSuccess(
            date = Instant.now(),
            customerReceipt = customerReceipt.orEmpty(),
            merchantReceipt = merchantReceipt.orEmpty(),
            rawData = intent?.extras?.toRawData().orEmpty(),
            data = intent?.extras?.toTransactionData(),
            reprintRequired = customerReceipt.isNullOrEmpty() && merchantReceipt.isNullOrEmpty()
        )

    private fun createError(intent: Intent?, customerReceipt: String?, merchantReceipt: String?, error: String?) =
        TerminalOperationError(
            date = Instant.now(),
            customerReceipt = customerReceipt.orEmpty(),
            merchantReceipt = merchantReceipt.orEmpty(),
            rawData = intent?.extras?.toRawData().orEmpty(),
            data = intent?.extras?.toTransactionData(),
            resultCode = ResultCodeSets.getSPOSCode(error),
            isRecoverable = RECOVERABLE_ERRORS.contains(error)
        )

    private fun Bundle.toTransactionData(): TransactionData =
        TransactionData(
            terminalType = Terminal.SPOS.TYPE,
            terminalId = getString(SPOSKey.ResultExtra.TERMINAL_ID).orEmpty(),
            transactionId = getString(SPOSKey.ResultExtra.TRANSACTION_DATA).orEmpty(),
            cardCircuit = getString(SPOSKey.ResultExtra.CARD_CIRCUIT).orEmpty(),
            cardPan = getString(SPOSKey.ResultExtra.CARD_PAN).orEmpty(),
            paymentProvider = getString(SPOSKey.ResultExtra.MERCHANT).orEmpty(), // TODO check if it is ok
        )

    private fun Bundle.getReceipt(key: String, converter: StringToReceiptDtoConverter): String =
        getString(key)?.let {
            converter.convert(it).toReceiptString()
        }.orEmpty()

    private fun SPOSResultState.isSuccessful(): Boolean =
        this == SPOSResultState.SUCCESS || this == SPOSResultState.PRINT_LAST_TICKET
}