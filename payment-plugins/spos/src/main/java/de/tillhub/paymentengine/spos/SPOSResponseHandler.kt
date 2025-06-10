package de.tillhub.paymentengine.spos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.data.TransactionData
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSResultCodes
import de.tillhub.paymentengine.spos.data.SPOSResultState
import de.tillhub.paymentengine.spos.data.SPOSTransactionResult
import de.tillhub.paymentengine.spos.data.SposTerminal
import de.tillhub.paymentengine.spos.data.StringToReceiptDtoConverter
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("TooManyFunctions")
internal object SPOSResponseHandler {

    private val RECOVERABLE_ERRORS = listOf("TERMINAL_CONNECTION_LOST", "RESPONSE_TIMEOUT")

    fun handleTerminalConnectResponse(
        resultCode: Int,
        intent: Intent?,
    ): TerminalOperationStatus.Login = if (resultCode == Activity.RESULT_OK) {
        TerminalOperationStatus.Login.Connected(
            date = Instant.now(),
            rawData = "",
            terminalType = SposTerminal.TYPE,
            terminalId = SposTerminal.TYPE,
        )
    } else {
        val error = intent?.extras?.getString(SPOSKey.ResultExtra.ERROR)
            ?: intent?.extras?.getString(SPOSKey.ResultExtra.ERROR_MESSAGE)

        error?.let {
            TerminalOperationStatus.Login.Error(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.getSPOSCode(it),
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
                    resultCode = SPOSResultCodes.getSPOSCode(error)
                )
            }
        } else {
            TerminalOperationStatus.TicketReprint.Canceled
        }

    fun <T : TerminalOperationStatus> handleTransactionResult(
        resultCode: Int,
        intent: Intent?,
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
                wrapSuccess(createSuccess(intent, customerReceipt, merchantReceipt), kClass)
            } else {
                wrapError(createError(intent, customerReceipt, merchantReceipt, error), kClass)
            }
        } else {
            error?.let {
                wrapError(createError(intent, customerReceipt, merchantReceipt, error), kClass)
            } ?: getCanceledStatus(kClass)
        }

        return operationStatus
    }

    private fun createSuccess(intent: Intent?, customerReceipt: String?, merchantReceipt: String?) =
        TerminalOperationSuccess(
            date = Instant.now(),
            customerReceipt = customerReceipt.orEmpty(),
            merchantReceipt = merchantReceipt.orEmpty(),
            rawData = intent?.extras?.toRawData().orEmpty(),
            data = intent?.extras?.toTransactionData(),
            reprintRequired = customerReceipt.isNullOrEmpty() && merchantReceipt.isNullOrEmpty()
        )

    private fun createError(
        intent: Intent?,
        customerReceipt: String?,
        merchantReceipt: String?,
        error: String?
    ) =
        TerminalOperationError(
            date = Instant.now(),
            customerReceipt = customerReceipt.orEmpty(),
            merchantReceipt = merchantReceipt.orEmpty(),
            rawData = intent?.extras?.toRawData().orEmpty(),
            data = intent?.extras?.toTransactionData(),
            resultCode = SPOSResultCodes.getSPOSCode(error),
            isRecoverable = RECOVERABLE_ERRORS.contains(error)
        )

    private fun Bundle.toTransactionData(): TransactionData =
        TransactionData(
            terminalType = SposTerminal.TYPE,
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

    private fun Bundle.toRawData(): String {
        val builder = StringBuilder()
        builder.appendLine("Extras {")

        keySet().forEach {
            builder.appendLine("$it = ${getString(it)}")
        }

        builder.append("}")
        return builder.toString()
    }

    private fun SPOSResultState.isSuccessful(): Boolean =
        this == SPOSResultState.SUCCESS || this == SPOSResultState.PRINT_LAST_TICKET

    private fun <T : TerminalOperationStatus> wrapSuccess(
        success: TerminalOperationSuccess,
        kClass: KClass<T>
    ) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Success(success)
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Success(success)
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Success(success)
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Success(
            success
        )
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Success(success)
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    private fun <T : TerminalOperationStatus> wrapError(
        error: TerminalOperationError,
        kClass: KClass<T>
    ) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Error(error)
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Error(error)
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Error(error)
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Error(
            error
        )
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Error(error)
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    private fun <T : TerminalOperationStatus> getCanceledStatus(kClass: KClass<T>) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Canceled
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Canceled
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Canceled
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Canceled
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Canceled
        TerminalOperationStatus.Login::class -> TerminalOperationStatus.Login.Canceled
        TerminalOperationStatus.TicketReprint::class -> TerminalOperationStatus.TicketReprint.Canceled
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    fun <T : TerminalOperationStatus> KClass<T>.getErrorAppNotFound() = when (this) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Error(
            TerminalOperationError(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
            )
        )
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Error(
            TerminalOperationError(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
            )
        )
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Error(
            TerminalOperationError(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
            )
        )
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Error(
            TerminalOperationError(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
            )
        )
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Error(
            TerminalOperationError(
                date = Instant.now(),
                rawData = "",
                resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
            )
        )
        TerminalOperationStatus.Login::class -> TerminalOperationStatus.Login.Error(
            date = Instant.now(),
            rawData = "",
            resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
        )
        TerminalOperationStatus.TicketReprint::class -> TerminalOperationStatus.TicketReprint.Error(
            date = Instant.now(),
            rawData = "",
            resultCode = SPOSResultCodes.APP_NOT_FOUND_ERROR
        )
        else -> throw IllegalArgumentException("Unknown status class: ${this.java.name}")
    }
}