package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionData
import de.tillhub.paymentengine.spos.SPOSResponseHandler.toRawData
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSResultState
import de.tillhub.paymentengine.spos.data.SPOSTransactionResult
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import java.time.Instant

internal object SPOSResponseHandler {
    fun handleTerminalConnectResponse(
        resultCode: Int,
        intent: Intent?,
        analytics: PaymentAnalytics? = null,
    ): TerminalOperationStatus = if (resultCode == Activity.RESULT_OK) {
        analytics?.logCommunication(
            protocol = PROTOCOL,
            message = "RESPONSE: RESULT OK"
        )
        TerminalOperationStatus.Success.SPOS(
            date = Instant.now(),
            customerReceipt = "",
            merchantReceipt = "",
            rawData = "",
            data = null
        )
    } else {
        analytics?.logCommunication(
            protocol = PROTOCOL,
            message = "RESPONSE: RESULT CANCELED\n${intent?.extras?.toRawData()}"
        )
        intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)?.let { errCode ->
            TerminalOperationStatus.Error.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = "",
                data = null,
                resultCode = ResultCodeSets.getSPOSCode(errCode)
            )
        } ?: TerminalOperationStatus.Canceled
    }

    fun handleTerminalDisconnectResponse(
        resultCode: Int,
        analytics: PaymentAnalytics? = null
    ): TerminalOperationStatus =
        if (resultCode == Activity.RESULT_OK) {
            analytics?.logCommunication(
                protocol = PROTOCOL,
                message = "RESPONSE: RESULT OK"
            )

            TerminalOperationStatus.Success.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = "",
                data = null
            )
        } else {
            analytics?.logCommunication(
                protocol = PROTOCOL,
                message = "RESPONSE: RESULT CANCELED"
            )
            TerminalOperationStatus.Canceled
        }

    fun handleTransactionResponse(
        resultCode: Int,
        intent: Intent?,
        analytics: PaymentAnalytics? = null,
        converter: StringToReceiptDtoConverter = StringToReceiptDtoConverter()
    ): TerminalOperationStatus = if (resultCode == Activity.RESULT_OK) {
        analytics?.logCommunication(
            protocol = PROTOCOL,
            message = "RESPONSE: RESULT OK\n${intent?.extras?.toRawData()}"
        )

        intent?.extras?.let { extras ->
            val merchantReceipt = extras.getReceipt(SPOSKey.ResultExtra.RECEIPT_MERCHANT, converter)
            val customerReceipt = extras.getReceipt(SPOSKey.ResultExtra.RECEIPT_CUSTOMER, converter)
            val resultState = SPOSResultState.find(
                type = extras.getString(SPOSKey.ResultExtra.RESULT_STATE).orEmpty()
            )
            val transactionResult = SPOSTransactionResult.find(
                type = extras.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT).orEmpty()
            )

            if (transactionResult == SPOSTransactionResult.ACCEPTED &&
                resultState == SPOSResultState.SUCCESS
                ) {
                TerminalOperationStatus.Success.SPOS(
                    date = Instant.now(),
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    rawData = extras.toRawData(),
                    data = extras.toTransactionData(),
                )
            } else {
                TerminalOperationStatus.Error.SPOS(
                    date = Instant.now(),
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    rawData = extras.toRawData(),
                    data = extras.toTransactionData(),
                    resultCode = ResultCodeSets.getSPOSCode(resultState.value)
                )
            }
        } ?: TerminalOperationStatus.Error.SPOS(
            date = Instant.now(),
            customerReceipt = "",
            merchantReceipt = "",
            rawData = "",
            data = null,
            resultCode = ResultCodeSets.getSPOSCode(null)
        )
    } else {
        analytics?.logCommunication(
            protocol = PROTOCOL,
            message = "RESPONSE: RESULT CANCELED\n${intent?.extras?.toRawData()}"
        )

        intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)?.let { errCode ->
            TerminalOperationStatus.Error.SPOS(
                date = Instant.now(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = intent.extras?.toRawData().orEmpty(),
                data = intent.extras?.toTransactionData(),
                resultCode = ResultCodeSets.getSPOSCode(errCode)
            )
        } ?: TerminalOperationStatus.Canceled
    }

    private fun Bundle.toRawData(): String {
        val builder = StringBuilder()
        builder.appendLine("Extras {")

        keySet().forEach {
            builder.appendLine("$it = ${getString(it)}")
        }

        builder.append("}")
        return builder.toString()
    }

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

    private const val PROTOCOL = "SPOS"
}