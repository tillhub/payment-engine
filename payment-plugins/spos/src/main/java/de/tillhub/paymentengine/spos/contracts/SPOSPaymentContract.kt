package de.tillhub.paymentengine.spos.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler

internal class SPOSPaymentContract : ActivityResultContract<PaymentRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: PaymentRequest): Intent =
        SPOSIntentFactory.createPaymentIntent(input)

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        SPOSResponseHandler.handleTransactionResult(
            resultCode = resultCode,
            intent = intent,
            kClass = TerminalOperationStatus.Payment::class
        )
}