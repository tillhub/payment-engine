package de.tillhub.paymentengine.helper

import android.app.Activity
import android.content.Intent
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import kotlin.reflect.KClass

internal object ResponseHandler {

    internal fun <T : TerminalOperationStatus>parseResult(
        resultCode: Int,
        intent: Intent?,
        analytics: PaymentAnalytics?,
        kClass: KClass<T>
    ): TerminalOperationStatus {
        return if (SPOSResponseHandler.canResolveTransactionResult(intent)) {
            SPOSResponseHandler.handleTransactionResult(resultCode, intent, analytics, kClass)
        } else {
            if (resultCode == Activity.RESULT_OK) {
                intent?.extras?.let {
                    BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
                } ?: getCanceledStatus(kClass)
            } else {
                getCanceledStatus(kClass)
            }
        }
    }

    internal fun <T : TerminalOperationStatus>getCanceledStatus(kClass: KClass<T>) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Canceled
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Canceled
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Canceled
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Canceled
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Canceled
        TerminalOperationStatus.Login::class -> TerminalOperationStatus.Login.Canceled
        TerminalOperationStatus.TicketReprint::class -> TerminalOperationStatus.TicketReprint.Canceled
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    internal fun <T : TerminalOperationStatus>wrapSuccess(
        success: TerminalOperationSuccess,
        kClass: KClass<T>
    ) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Success(success)
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Success(success)
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Success(success)
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Success(success)
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Success(success)
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    internal fun <T : TerminalOperationStatus>wrapError(
        error: TerminalOperationError,
        kClass: KClass<T>
    ) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Error(error)
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Error(error)
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Error(error)
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Error(error)
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Error(error)
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    internal fun isSuccess(status: TerminalOperationStatus) = listOf(
        TerminalOperationStatus.Payment.Success::class,
        TerminalOperationStatus.Reversal.Success::class,
        TerminalOperationStatus.Refund.Success::class,
        TerminalOperationStatus.Reconciliation.Success::class,
        TerminalOperationStatus.Recovery.Success::class,
        TerminalOperationStatus.TicketReprint.Success::class
    ).any { it == status::class }
}