package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.Instant
import java.util.Objects

@Parcelize
sealed class TerminalOperationStatus : Parcelable {
    data object Waiting : TerminalOperationStatus()

    @Parcelize
    sealed class Payment : TerminalOperationStatus() {
        class Pending(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Payment() {

            override fun equals(other: Any?) = other is Pending &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Payment.Pending(amount=$amount, currency=$currency)"
        }
        class Success(val response: TerminalOperationSuccess) : Payment()
        class Error(val response: TerminalOperationError) : Payment()
        data object Canceled : Payment()
    }

    @Parcelize
    sealed class Reversal : TerminalOperationStatus() {
        class Pending(val receiptNo: String) : Reversal() {
            override fun equals(other: Any?) = other is Pending &&
                    receiptNo == other.receiptNo

            override fun hashCode() = Objects.hash(receiptNo)
            override fun toString() = "Reversal.Pending(receiptNo=$receiptNo)"
        }
        class Success(val response: TerminalOperationSuccess) : Reversal()
        class Error(val response: TerminalOperationError) : Reversal()
        data object Canceled : Reversal()
    }

    @Parcelize
    sealed class Refund : TerminalOperationStatus() {
        class Pending(val amount: BigDecimal, val currency: ISOAlphaCurrency) : Refund() {
            override fun equals(other: Any?) = other is Pending &&
                    amount == other.amount &&
                    currency == other.currency

            override fun hashCode() = Objects.hash(amount, currency)
            override fun toString() = "Refund.Pending(amount=$amount, currency=$currency)"
        }
        class Success(val response: TerminalOperationSuccess) : Refund()
        class Error(val response: TerminalOperationError) : Refund()
        data object Canceled : Refund()
    }

    @Parcelize
    sealed class Reconciliation : TerminalOperationStatus() {
        data object Pending : Reconciliation()
        class Success(val response: TerminalOperationSuccess) : Reconciliation()
        class Error(val response: TerminalOperationError) : Reconciliation()
        data object Canceled : Reconciliation()
    }

    @Parcelize
    sealed class Recovery : TerminalOperationStatus() {
        data object Pending : Recovery()
        class Success(val response: TerminalOperationSuccess) : Recovery()
        class Error(val response: TerminalOperationError) : Recovery()
        data object Canceled : Recovery()
    }

    @Parcelize
    sealed class Login : TerminalOperationStatus() {
        data object Pending : Login()
        class Connected(
            val date: Instant,
            val rawData: String,
            val terminalType: String,
            val terminalId: String
        ) : Login()
        class Disconnected(
            val date: Instant,
        ) : Login()
        class Error(
            val date: Instant,
            val rawData: String,
            val resultCode: TransactionResultCode,
        ) : Login()
        data object Canceled : Login()
    }

    @Parcelize
    sealed class TicketReprint : TerminalOperationStatus() {
        data object Pending : TicketReprint()
        class Success(
            val date: Instant,
            val customerReceipt: String = "",
            val merchantReceipt: String = "",
            val rawData: String = "",
        ) : TicketReprint()
        class Error(
            val date: Instant,
            val rawData: String = "",
            val resultCode: TransactionResultCode,
        ) : TicketReprint()
        data object Canceled : TicketReprint()
    }
}
