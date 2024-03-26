package de.tillhub.paymentengine.opi.data

import de.tillhub.paymentengine.data.TerminalOperationStatus
import java.time.Instant

sealed class OPIOperationStatus {
    sealed class Pending : OPIOperationStatus() {
        data object NoMessage: Pending()
        data class WithMessage(val message: String): Pending()
    }

    sealed class Error : OPIOperationStatus() {
        data object NotInitialised: Error()
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