package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSResultState
import de.tillhub.paymentengine.spos.data.SPOSTransactionResult
import java.time.Instant

internal object SPOSResponseHandler {
    fun handleTerminalConnectResponse(
        resultCode: Int,
        intent: Intent?
    ): TerminalOperationStatus = if (resultCode == Activity.RESULT_OK) {
        TerminalOperationStatus.Success.SPOS(
            date = Instant.now(),
            customerReceipt = "",
            merchantReceipt = "",
            rawData = "",
            data = null
        )
    } else {
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

    fun handleTerminalDisconnectResponse(resultCode: Int): TerminalOperationStatus =
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

    fun handleTransactionResponse(
        resultCode: Int,
        intent: Intent?
    ): TerminalOperationStatus = if (resultCode == Activity.RESULT_OK) {
        intent?.extras?.let { extras ->
            val merchantReceipt = extras.getString(SPOSKey.ResultExtra.RECEIPT_MERCHANT).orEmpty()
            val customerReceipt = extras.getString(SPOSKey.ResultExtra.RECEIPT_CUSTOMER).orEmpty()
            val resultState = SPOSResultState.find(
                type = extras.getString(SPOSKey.ResultExtra.RESULT_STATE).orEmpty()
            )
            val transactionResult = SPOSTransactionResult.find(
                type = extras.getString(SPOSKey.ResultExtra.TRANSACTION_RESULT).orEmpty()
            )

            if (transactionResult == SPOSTransactionResult.ACCEPTED &&
                resultState == SPOSResultState.SUCCESS) {
                TerminalOperationStatus.Success.SPOS(
                    date = Instant.now(),
                    customerReceipt = merchantReceipt,
                    merchantReceipt = customerReceipt,
                    rawData = extras.toRawData(),
                    data = null, // TODO
                )
            } else {
                TerminalOperationStatus.Error.SPOS(
                    date = Instant.now(),
                    customerReceipt = merchantReceipt,
                    merchantReceipt = customerReceipt,
                    rawData = extras.toRawData(),
                    data = null,
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

    private fun Bundle.toRawData(): String {
        val builder = StringBuilder()
        builder.appendLine("Extras {")

        keySet().forEach {
            builder.appendLine("$it = ${getString(it)}")
        }

        builder.append("}")
        return builder.toString()
    }
}