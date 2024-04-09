package de.tillhub.paymentengine.opi.data

import de.tillhub.paymentengine.data.TerminalOperationStatus
import java.time.Instant

sealed class OPIOperationStatus {
    data object Idle : OPIOperationStatus()

    data class Pending(
        val date: Instant,
        val messageLines: List<String> = emptyList(),
        val customerReceipt: String? = null,
        val merchantReceipt: String? = null,
    ) : OPIOperationStatus()

    sealed class Error : OPIOperationStatus() {
        data object NotInitialised : Error()
        data class Result(
            val date: Instant,
            val customerReceipt: String,
            val merchantReceipt: String,
            val rawData: String,
            val data: CardServiceResponse?
        ) : Error()

        fun toTerminalOperation() = when (this) {
            NotInitialised -> TODO()
            is Result -> TerminalOperationStatus.Error.OPI(
                date, customerReceipt, merchantReceipt, rawData
            )
        }
    }

    data class Success(
        val date: Instant,
        val customerReceipt: String,
        val merchantReceipt: String,
        val rawData: String,
        val data: CardServiceResponse?
    ) : OPIOperationStatus() {
        fun toTerminalOperation() =
            TerminalOperationStatus.Success.OPI(
                date, customerReceipt, merchantReceipt, rawData
            )
    }
}