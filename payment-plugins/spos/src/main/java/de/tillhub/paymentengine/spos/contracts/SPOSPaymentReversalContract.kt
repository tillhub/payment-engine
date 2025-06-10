package de.tillhub.paymentengine.spos.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler

internal class SPOSPaymentReversalContract : ActivityResultContract<ReversalRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: ReversalRequest): Intent =
        SPOSIntentFactory.createPaymentReversalIntent(input)

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        SPOSResponseHandler.handleTransactionResult(
            resultCode = resultCode,
            intent = intent,
            kClass = TerminalOperationStatus.Reversal::class
        )
}