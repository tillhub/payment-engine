package de.tillhub.paymentengine.opi.data

import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.TerminalOperationStatus
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
        abstract val reconciliationData: ServiceResponse?

        abstract fun toTerminalOperation(): TerminalOperationStatus

        data class Error(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: CardServiceResponse? = null,
            override val reconciliationData: ServiceResponse? = null
        ) : Result() {
            override fun toTerminalOperation() =
                TerminalOperationStatus.Error.OPI(
                    date = date,
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    rawData = rawData,
                    data = data?.let {
                        TransactionData(
                            terminalId = it.terminal?.terminalId.orEmpty(),
                            transactionId = it.terminal?.stan.orEmpty(),
                            cardCircuit = it.cardValue?.cardCircuit?.value.orEmpty(),
                            cardPan = it.cardValue?.cardPAN?.value.orEmpty(),
                            paymentProvider = it.tender?.authorisation?.acquirerID.orEmpty()
                        )
                    } ?: reconciliationData?.let {
                        TransactionData(
                            terminalId = it.terminal?.terminalId.orEmpty(),
                            transactionId = "",
                            cardCircuit = "",
                            cardPan = "",
                            paymentProvider = it.authorisation?.acquirerID.orEmpty()
                        )
                    },
                    resultCode = ResultCodeSets.getOPICode(
                        resultCode = data?.tender?.authorisation?.actionCode?.toIntOrNull()
                            ?: reconciliationData?.privateData?.errorCode?.value?.toIntOrNull()
                    )
                )
        }

        data class Success(
            override val date: Instant,
            override val customerReceipt: String,
            override val merchantReceipt: String,
            override val rawData: String,
            override val data: CardServiceResponse? = null,
            override val reconciliationData: ServiceResponse? = null
        ) : Result() {
            override fun toTerminalOperation() =
                TerminalOperationStatus.Success.OPI(
                    date = date,
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    rawData = rawData,
                    data = data?.let {
                        TransactionData(
                            terminalId = it.terminal?.terminalId.orEmpty(),
                            transactionId = it.terminal?.stan.orEmpty(),
                            cardCircuit = it.cardValue?.cardCircuit?.value.orEmpty(),
                            cardPan = it.cardValue?.cardPAN?.value.orEmpty(),
                            paymentProvider = it.tender?.authorisation?.acquirerID.orEmpty()
                        )
                    } ?: reconciliationData?.let {
                        TransactionData(
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