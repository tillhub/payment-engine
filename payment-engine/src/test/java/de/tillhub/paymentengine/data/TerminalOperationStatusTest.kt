package de.tillhub.paymentengine.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.time.Instant

class TerminalOperationStatusTest : FunSpec({
    val successResponse: TerminalOperationSuccess = mockk()
    val errorResponse: TerminalOperationError = mockk()

    val date: Instant = mockk()
    val resultCode: TransactionResultCode = mockk()

    test("isPending") {
        TerminalOperationStatus.Waiting.isPending().shouldBeFalse()

        // PAYMENT
        TerminalOperationStatus.Payment.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isPending().shouldBeTrue()
        TerminalOperationStatus.Payment.Error(errorResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Payment.Success(successResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Payment.Canceled.isPending().shouldBeFalse()

        // REVERSAL
        TerminalOperationStatus.Reversal.Pending("receiptNo").isPending().shouldBeTrue()
        TerminalOperationStatus.Reversal.Error(errorResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Reversal.Success(successResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Reversal.Canceled.isPending().shouldBeFalse()

        // REFUND
        TerminalOperationStatus.Refund.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isPending().shouldBeTrue()
        TerminalOperationStatus.Refund.Error(errorResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Refund.Success(successResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Refund.Canceled.isPending().shouldBeFalse()

        // RECONCILIATION
        TerminalOperationStatus.Reconciliation.Pending.isPending().shouldBeTrue()
        TerminalOperationStatus.Reconciliation.Error(errorResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Success(successResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Canceled.isPending().shouldBeFalse()

        // RECOVERY
        TerminalOperationStatus.Recovery.Pending.isPending().shouldBeTrue()
        TerminalOperationStatus.Recovery.Error(errorResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Recovery.Success(successResponse).isPending().shouldBeFalse()
        TerminalOperationStatus.Recovery.Canceled.isPending().shouldBeFalse()

        // LOGIN
        TerminalOperationStatus.Login.Pending.isPending().shouldBeTrue()
        TerminalOperationStatus.Login.Error(date, "rawData", resultCode).isPending().shouldBeFalse()
        TerminalOperationStatus.Login.Connected(
            date,
            "rawData",
            "ZVT",
            "terminalId"
        ).isPending().shouldBeFalse()
        TerminalOperationStatus.Login.Disconnected(date).isPending().shouldBeFalse()
        TerminalOperationStatus.Login.Canceled.isPending().shouldBeFalse()

        // TICKET REPRINT
        TerminalOperationStatus.TicketReprint.Pending.isPending().shouldBeTrue()
        TerminalOperationStatus.TicketReprint.Error(date, "rawData", resultCode).isPending().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Success(date).isPending().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Canceled.isPending().shouldBeFalse()
    }

    test("isCanceled") {
        TerminalOperationStatus.Waiting.isCanceled().shouldBeFalse()

        // PAYMENT
        TerminalOperationStatus.Payment.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Payment.Error(errorResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Payment.Success(successResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Payment.Canceled.isCanceled().shouldBeTrue()

        // REVERSAL
        TerminalOperationStatus.Reversal.Pending("receiptNo").isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reversal.Error(errorResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reversal.Success(successResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reversal.Canceled.isCanceled().shouldBeTrue()

        // REFUND
        TerminalOperationStatus.Refund.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Refund.Error(errorResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Refund.Success(successResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Refund.Canceled.isCanceled().shouldBeTrue()

        // RECONCILIATION
        TerminalOperationStatus.Reconciliation.Pending.isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Error(errorResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Success(successResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Canceled.isCanceled().shouldBeTrue()

        // RECOVERY
        TerminalOperationStatus.Recovery.Pending.isCanceled().shouldBeFalse()
        TerminalOperationStatus.Recovery.Error(errorResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Recovery.Success(successResponse).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Recovery.Canceled.isCanceled().shouldBeTrue()

        // LOGIN
        TerminalOperationStatus.Login.Pending.isCanceled().shouldBeFalse()
        TerminalOperationStatus.Login.Error(date, "rawData", resultCode).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Login.Connected(
            date,
            "rawData",
            "ZVT",
            "terminalId"
        ).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Login.Disconnected(date).isCanceled().shouldBeFalse()
        TerminalOperationStatus.Login.Canceled.isCanceled().shouldBeTrue()

        // TICKET REPRINT
        TerminalOperationStatus.TicketReprint.Pending.isCanceled().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Error(date, "rawData", resultCode).isCanceled().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Success(date).isCanceled().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Canceled.isCanceled().shouldBeTrue()
    }

    test("isError") {
        TerminalOperationStatus.Waiting.isError().shouldBeFalse()

        // PAYMENT
        TerminalOperationStatus.Payment.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isError().shouldBeFalse()
        TerminalOperationStatus.Payment.Error(errorResponse).isError().shouldBeTrue()
        TerminalOperationStatus.Payment.Success(successResponse).isError().shouldBeFalse()
        TerminalOperationStatus.Payment.Canceled.isError().shouldBeFalse()

        // REVERSAL
        TerminalOperationStatus.Reversal.Pending("receiptNo").isError().shouldBeFalse()
        TerminalOperationStatus.Reversal.Error(errorResponse).isError().shouldBeTrue()
        TerminalOperationStatus.Reversal.Success(successResponse).isError().shouldBeFalse()
        TerminalOperationStatus.Reversal.Canceled.isError().shouldBeFalse()

        // REFUND
        TerminalOperationStatus.Refund.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isError().shouldBeFalse()
        TerminalOperationStatus.Refund.Error(errorResponse).isError().shouldBeTrue()
        TerminalOperationStatus.Refund.Success(successResponse).isError().shouldBeFalse()
        TerminalOperationStatus.Refund.Canceled.isError().shouldBeFalse()

        // RECONCILIATION
        TerminalOperationStatus.Reconciliation.Pending.isError().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Error(errorResponse).isError().shouldBeTrue()
        TerminalOperationStatus.Reconciliation.Success(successResponse).isError().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Canceled.isError().shouldBeFalse()

        // RECOVERY
        TerminalOperationStatus.Recovery.Pending.isError().shouldBeFalse()
        TerminalOperationStatus.Recovery.Error(errorResponse).isError().shouldBeTrue()
        TerminalOperationStatus.Recovery.Success(successResponse).isError().shouldBeFalse()
        TerminalOperationStatus.Recovery.Canceled.isError().shouldBeFalse()

        // LOGIN
        TerminalOperationStatus.Login.Pending.isError().shouldBeFalse()
        TerminalOperationStatus.Login.Error(date, "rawData", resultCode).isError().shouldBeTrue()
        TerminalOperationStatus.Login.Connected(
            date,
            "rawData",
            "ZVT",
            "terminalId"
        ).isError().shouldBeFalse()
        TerminalOperationStatus.Login.Disconnected(date).isError().shouldBeFalse()
        TerminalOperationStatus.Login.Canceled.isError().shouldBeFalse()

        // TICKET REPRINT
        TerminalOperationStatus.TicketReprint.Pending.isError().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Error(date, "rawData", resultCode).isError().shouldBeTrue()
        TerminalOperationStatus.TicketReprint.Success(date).isError().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Canceled.isError().shouldBeFalse()
    }

    test("isSuccess") {
        TerminalOperationStatus.Waiting.isSuccess().shouldBeFalse()

        // PAYMENT
        TerminalOperationStatus.Payment.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Payment.Error(errorResponse).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Payment.Success(successResponse).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Payment.Canceled.isSuccess().shouldBeFalse()

        // REVERSAL
        TerminalOperationStatus.Reversal.Pending("receiptNo").isSuccess().shouldBeFalse()
        TerminalOperationStatus.Reversal.Error(errorResponse).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Reversal.Success(successResponse).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Reversal.Canceled.isSuccess().shouldBeFalse()

        // REFUND
        TerminalOperationStatus.Refund.Pending(
            1.0.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        ).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Refund.Error(errorResponse).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Refund.Success(successResponse).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Refund.Canceled.isSuccess().shouldBeFalse()

        // RECONCILIATION
        TerminalOperationStatus.Reconciliation.Pending.isSuccess().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Error(errorResponse).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Reconciliation.Success(successResponse).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Reconciliation.Canceled.isSuccess().shouldBeFalse()

        // RECOVERY
        TerminalOperationStatus.Recovery.Pending.isSuccess().shouldBeFalse()
        TerminalOperationStatus.Recovery.Error(errorResponse).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Recovery.Success(successResponse).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Recovery.Canceled.isSuccess().shouldBeFalse()

        // LOGIN
        TerminalOperationStatus.Login.Pending.isSuccess().shouldBeFalse()
        TerminalOperationStatus.Login.Error(date, "rawData", resultCode).isSuccess().shouldBeFalse()
        TerminalOperationStatus.Login.Connected(
            date,
            "rawData",
            "ZVT",
            "terminalId"
        ).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Login.Disconnected(date).isSuccess().shouldBeTrue()
        TerminalOperationStatus.Login.Canceled.isSuccess().shouldBeFalse()

        // TICKET REPRINT
        TerminalOperationStatus.TicketReprint.Pending.isSuccess().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Error(date, "rawData", resultCode).isSuccess().shouldBeFalse()
        TerminalOperationStatus.TicketReprint.Success(date).isSuccess().shouldBeTrue()
        TerminalOperationStatus.TicketReprint.Canceled.isSuccess().shouldBeFalse()
    }
})
