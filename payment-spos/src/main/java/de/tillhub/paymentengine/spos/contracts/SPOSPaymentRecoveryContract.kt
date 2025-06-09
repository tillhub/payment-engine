package de.tillhub.paymentengine.spos.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSTerminal

internal class SPOSPaymentRecoveryContract : ActivityResultContract<SPOSTerminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: SPOSTerminal): Intent =
        SPOSIntentFactory.createRecoveryIntent()

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        SPOSResponseHandler.handleTransactionResult(
            resultCode = resultCode,
            intent = intent,
            kClass = TerminalOperationStatus.Recovery::class
        )
}