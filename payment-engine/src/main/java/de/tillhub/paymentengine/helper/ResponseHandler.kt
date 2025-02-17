package de.tillhub.paymentengine.helper

import android.app.Activity
import android.content.Intent
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSResponseHandler

internal object ResponseHandler {

    internal fun parseResult(resultCode: Int, intent: Intent?, analytics: PaymentAnalytics?): TerminalOperationStatus {
        return if (SPOSResponseHandler.canResolveTransactionResult(intent)) {
            SPOSResponseHandler.handleTransactionResult(resultCode, intent, analytics)
        } else {
            if (resultCode == Activity.RESULT_OK) {
                intent?.extras?.let {
                    BundleCompat.getParcelable(it, ExtraKeys.EXTRAS_RESULT, TerminalOperationStatus::class.java)
                } ?: TerminalOperationStatus.Canceled
            } else {
                TerminalOperationStatus.Canceled
            }
        }
    }
}