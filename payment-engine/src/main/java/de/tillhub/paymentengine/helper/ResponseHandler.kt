package de.tillhub.paymentengine.helper

import android.app.Activity
import android.content.Intent
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlin.reflect.KClass

internal object ResponseHandler {

    internal fun <T : TerminalOperationStatus>parseResult(
        resultCode: Int,
        intent: Intent?,
        kClass: KClass<T>
    ): TerminalOperationStatus {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.extras?.let {
                BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
            } ?: getCanceledStatus(kClass)
        } else {
            getCanceledStatus(kClass)
        }
    }

    private fun <T : TerminalOperationStatus>getCanceledStatus(kClass: KClass<T>) = when (kClass) {
        TerminalOperationStatus.Payment::class -> TerminalOperationStatus.Payment.Canceled
        TerminalOperationStatus.Reversal::class -> TerminalOperationStatus.Reversal.Canceled
        TerminalOperationStatus.Refund::class -> TerminalOperationStatus.Refund.Canceled
        TerminalOperationStatus.Reconciliation::class -> TerminalOperationStatus.Reconciliation.Canceled
        TerminalOperationStatus.Recovery::class -> TerminalOperationStatus.Recovery.Canceled
        TerminalOperationStatus.Login::class -> TerminalOperationStatus.Login.Canceled
        TerminalOperationStatus.TicketReprint::class -> TerminalOperationStatus.TicketReprint.Canceled
        else -> throw IllegalArgumentException("Unknown status class: ${kClass.java.name}")
    }

    internal fun isSuccess(status: TerminalOperationStatus) = listOf(
        TerminalOperationStatus.Payment.Success::class,
        TerminalOperationStatus.Reversal.Success::class,
        TerminalOperationStatus.Refund.Success::class,
        TerminalOperationStatus.Reconciliation.Success::class,
        TerminalOperationStatus.Recovery.Success::class,
        TerminalOperationStatus.TicketReprint.Success::class,
        TerminalOperationStatus.Login.Connected::class,
        TerminalOperationStatus.Login.Disconnected::class
    ).any { it == status::class }

    internal fun isError(status: TerminalOperationStatus) = listOf(
        TerminalOperationStatus.Payment.Error::class,
        TerminalOperationStatus.Reversal.Error::class,
        TerminalOperationStatus.Refund.Error::class,
        TerminalOperationStatus.Reconciliation.Error::class,
        TerminalOperationStatus.Recovery.Error::class,
        TerminalOperationStatus.TicketReprint.Error::class,
        TerminalOperationStatus.Login.Error::class,
    ).any { it == status::class }
}