package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionData
import de.tillhub.paymentengine.spos.SPOSResponseHandler.toRawData
import de.tillhub.paymentengine.spos.SPOSResponseHandler.toTransactionData
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSResultState
import de.tillhub.paymentengine.spos.data.SPOSTransactionResult
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import java.time.Instant

internal object SPOSResponseHandler {

    private const val SPOS_PROTOCOL = "SPOS"
    private val RECOVERABLE_ERRORS = listOf("TERMINAL_CONNECTION_LOST", "RESPONSE_TIMEOUT")

    fun handleTerminalConnectResponse(
        resultCode: Int,
        intent: Intent?,
    ): TerminalOperationStatus = if (resultCode == Activity.RESULT_OK) {
        TerminalOperationStatus.Success.SPOS(
            date = Instant.now(),
            customerReceipt = "",
            merchantReceipt = "",
            rawData = "",
            data = null
        )
    } else {
        val error = intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)
            ?: intent?.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)

        error?.let {
            TerminalOperationStatus.Error.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = "",
                data = null,
                resultCode = ResultCodeSets.getSPOSCode(it),
                isRecoverable = false
            )
        } ?: TerminalOperationStatus.Canceled
    }

    fun handleTerminalDisconnectResponse(
        resultCode: Int,
    ): TerminalOperationStatus =
        if (resultCode == Activity.RESULT_OK) {
            TerminalOperationStatus.Success.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = "",
                data = null
            )
        } else {
            TerminalOperationStatus.Canceled
        }

    fun handleTransactionResult(
        resultCode: Int,
        intent: Intent?,
        analytics: PaymentAnalytics?,
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
                TerminalOperationStatus.Success.SPOS(
                    date = Instant.now(),
                    customerReceipt = customerReceipt.orEmpty(),
                    merchantReceipt = merchantReceipt.orEmpty(),
                    rawData = intent?.extras?.toRawData().orEmpty(),
                    data = intent?.extras?.toTransactionData(),
                )
            } else {
                createError(
                    intent = intent,
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    error = error
                )
            }
        } else {
            error?.let {
                createError(
                    intent = intent,
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    error = error
                )
            } ?: TerminalOperationStatus.Canceled
        }

        return operationStatus.also {
            when (it) {
                TerminalOperationStatus.Canceled,
                is TerminalOperationStatus.Error.SPOS -> analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.createResultCanceled(intent?.extras)
                )
                is TerminalOperationStatus.Success.SPOS -> analytics?.logCommunication(
                    protocol = SPOS_PROTOCOL,
                    message = AnalyticsMessageFactory.createResultOk(intent?.extras)
                )
                else -> throw IllegalStateException("Not possible Operation Status: $it")
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

    private fun createError(intent: Intent?, customerReceipt: String?, merchantReceipt: String?, error: String?) =
        TerminalOperationStatus.Error.SPOS(
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