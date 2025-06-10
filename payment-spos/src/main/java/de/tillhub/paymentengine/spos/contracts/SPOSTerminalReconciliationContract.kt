package de.tillhub.paymentengine.spos.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SposTerminal

internal class SPOSTerminalReconciliationContract : ActivityResultContract<SposTerminal, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: SposTerminal): Intent =
        SPOSIntentFactory.createReconciliationIntent()

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus =
        SPOSResponseHandler.handleTransactionResult(
            resultCode = resultCode,
            intent = intent,
            kClass = TerminalOperationStatus.Reconciliation::class
        )
}