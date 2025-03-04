package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.Instant
import java.util.Objects

@Parcelize
sealed class TerminalOperationStatus private constructor(
    private val type: StatusType
) : Parcelable {
    fun isPending(): Boolean = type == StatusType.PENDING
    fun isSuccess(): Boolean = type == StatusType.SUCCESS
    fun isError(): Boolean = type == StatusType.ERROR
    fun isCanceled(): Boolean = type == StatusType.CANCELED

    fun getSuccessData(): TerminalOperationSuccess? = when (this) {
        is Payment.Success -> response
        is Reversal.Success -> response
        is Refund.Success -> response
        is Reconciliation.Success -> response
        is Recovery.Success -> response

        else -> null
    }

    fun getErrorData(): TerminalOperationError? = when (this) {
        is Payment.Error -> response
        is Reversal.Error -> response
        is Refund.Error -> response
        is Reconciliation.Error -> response
        is Recovery.Error -> response

        else -> null
    }

    data object Waiting : TerminalOperationStatus(StatusType.WAITING)

    @Parcelize
    sealed class Payment private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        class Pending(
            val amount: BigDecimal,
            val currency: ISOAlphaCurrency
        ) : Payment(StatusType.PENDING) {
            override fun equals(other: Any?) = other is Pending &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Payment.Pending(amount=$amount, currency=$currency)"
        }
        class Success(val response: TerminalOperationSuccess) : Payment(StatusType.SUCCESS)
        class Error(val response: TerminalOperationError) : Payment(StatusType.ERROR)
        data object Canceled : Payment(StatusType.CANCELED)
    }

    @Parcelize
    sealed class Reversal private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        class Pending(val receiptNo: String) : Reversal(StatusType.PENDING) {
            override fun equals(other: Any?) = other is Pending &&
                    receiptNo == other.receiptNo

            override fun hashCode() = Objects.hash(receiptNo)
            override fun toString() = "Reversal.Pending(receiptNo=$receiptNo)"
        }
        class Success(val response: TerminalOperationSuccess) : Reversal(StatusType.SUCCESS)
        class Error(val response: TerminalOperationError) : Reversal(StatusType.ERROR)
        data object Canceled : Reversal(StatusType.CANCELED)
    }

    @Parcelize
    sealed class Refund private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        class Pending(
            val amount: BigDecimal,
            val currency: ISOAlphaCurrency
        ) : Refund(StatusType.PENDING) {
            override fun equals(other: Any?) = other is Pending &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Refund.Pending(amount=$amount, currency=$currency)"
        }
        class Success(val response: TerminalOperationSuccess) : Refund(StatusType.SUCCESS)
        class Error(val response: TerminalOperationError) : Refund(StatusType.ERROR)
        data object Canceled : Refund(StatusType.CANCELED)
    }

    @Parcelize
    sealed class Reconciliation private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        data object Pending : Reconciliation(StatusType.PENDING)
        class Success(val response: TerminalOperationSuccess) : Reconciliation(StatusType.SUCCESS)
        class Error(val response: TerminalOperationError) : Reconciliation(StatusType.ERROR)
        data object Canceled : Reconciliation(StatusType.CANCELED)
    }

    @Parcelize
    sealed class Recovery private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        data object Pending : Recovery(StatusType.PENDING)
        class Success(val response: TerminalOperationSuccess) : Recovery(StatusType.SUCCESS)
        class Error(val response: TerminalOperationError) : Recovery(StatusType.ERROR)
        data object Canceled : Recovery(StatusType.CANCELED)
    }

    @Parcelize
    sealed class Login private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        data object Pending : Login(StatusType.PENDING)
        class Connected(
            val date: Instant,
            val rawData: String,
            val terminalType: String,
            val terminalId: String
        ) : Login(StatusType.SUCCESS)
        class Disconnected(
            val date: Instant,
        ) : Login(StatusType.SUCCESS)
        class Error(
            val date: Instant,
            val rawData: String,
            val resultCode: TransactionResultCode,
        ) : Login(StatusType.ERROR)
        data object Canceled : Login(StatusType.CANCELED)
    }

    @Parcelize
    sealed class TicketReprint private constructor(
        private val type: StatusType
    ) : TerminalOperationStatus(type) {
        data object Pending : TicketReprint(StatusType.PENDING)
        class Success(
            val date: Instant,
            val customerReceipt: String = "",
            val merchantReceipt: String = "",
            val rawData: String = "",
        ) : TicketReprint(StatusType.SUCCESS)
        class Error(
            val date: Instant,
            val rawData: String = "",
            val resultCode: TransactionResultCode,
        ) : TicketReprint(StatusType.ERROR)
        data object Canceled : TicketReprint(StatusType.CANCELED)
    }
}

internal enum class StatusType {
    WAITING, PENDING, SUCCESS, ERROR, CANCELED
}