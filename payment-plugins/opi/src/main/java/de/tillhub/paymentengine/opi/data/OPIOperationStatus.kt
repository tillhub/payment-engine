package de.tillhub.paymentengine.opi.data

import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.data.TransactionData
import java.time.Instant

internal sealed class OPIOperationStatus {
    data object Idle : OPIOperationStatus()

    sealed class Pending : OPIOperationStatus() {
        data object Login : Pending()
        data class Operation(
            val date: Instant,
            val messageLines: List<String> = emptyList(),
            val customerReceipt: String? = null,
            val merchantReceipt: String? = null,
        ) : Pending()
    }

    data object LoggedIn : OPIOperationStatus()

    sealed class Error(open val message: String) : OPIOperationStatus() {
        data object NotInitialised : Error("OPI Communication controller not initialised.")
        data class Communication(
            override val message: String,
            val error: Throwable?,
        ) : Error(message)
        data class DataHandling(
            override val message: String,
            val error: Throwable,
        ) : Error(message)
    }

    sealed class Result : OPIOperationStatus() {
        abstract val date: Instant
        abstract val customerReceipt: String
        abstract val merchantReceipt: String
        abstract val rawData: String
        abstract val data: CardServiceResponse?
        abstract val serviceData: ServiceResponse?

        data class Error(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: CardServiceResponse? = null,
            override val serviceData: ServiceResponse? = null
        ) : Result() {
            fun toTerminalOperation() = TerminalOperationError(
                date = date,
                customerReceipt = customerReceipt,
                merchantReceipt = merchantReceipt,
                rawData = rawData,
                data = data?.let {
                    TransactionData(
                        terminalType = OpiTerminal.TYPE,
                        terminalId = it.terminal?.terminalId.orEmpty(),
                        transactionId = it.terminal?.stan.orEmpty(),
                        cardCircuit = it.cardValue?.cardCircuit?.value.orEmpty(),
                        cardPan = it.cardValue?.cardPAN?.value.orEmpty(),
                        paymentProvider = it.tender?.authorisation?.acquirerID.orEmpty()
                    )
                } ?: serviceData?.let {
                    TransactionData(
                        terminalType = OpiTerminal.TYPE,
                        terminalId = it.terminal?.terminalId.orEmpty(),
                        transactionId = "",
                        cardCircuit = "",
                        cardPan = "",
                        paymentProvider = it.authorisation?.acquirerID.orEmpty()
                    )
                },
                resultCode = OpiResultCodes.getOPICode(
                    resultCode = data?.tender?.authorisation?.actionCode?.toIntOrNull()
                        ?: serviceData?.privateData?.errorCode?.value?.toIntOrNull()
                )
            )
        }

        data class Success(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: CardServiceResponse? = null,
            override val serviceData: ServiceResponse? = null
        ) : Result() {
            fun toTerminalOperation() = TerminalOperationSuccess(
                date = date,
                customerReceipt = customerReceipt,
                merchantReceipt = merchantReceipt,
                rawData = rawData,
                data = data?.let {
                    TransactionData(
                        terminalType = OpiTerminal.TYPE,
                        terminalId = it.terminal?.terminalId.orEmpty(),
                        transactionId = it.terminal?.stan.orEmpty(),
                        cardCircuit = it.cardValue?.cardCircuit?.value.orEmpty(),
                        cardPan = it.cardValue?.cardPAN?.value.orEmpty(),
                        paymentProvider = it.tender?.authorisation?.acquirerID.orEmpty()
                    )
                } ?: serviceData?.let {
                    TransactionData(
                        terminalType = OpiTerminal.TYPE,
                        terminalId = it.terminal?.terminalId.orEmpty(),
                        transactionId = "",
                        cardCircuit = "",
                        cardPan = "",
                        paymentProvider = it.authorisation?.acquirerID.orEmpty()
                    )
                }
            )
        }
    }
}