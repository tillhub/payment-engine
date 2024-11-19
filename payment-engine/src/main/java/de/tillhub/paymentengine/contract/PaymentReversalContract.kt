package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.ui.OPIPaymentReversalActivity
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import de.tillhub.paymentengine.spos.SPOSResponseHandler
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.zvt.ui.CardPaymentReversalActivity
import java.math.BigDecimal
import java.util.Objects

class PaymentReversalContract : ActivityResultContract<ReversalRequest, TerminalOperationStatus>() {

    override fun createIntent(context: Context, input: ReversalRequest): Intent {
        return when (input.config) {
            is Terminal.ZVT -> Intent(context, CardPaymentReversalActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
            }

            is Terminal.OPI -> Intent(context, OPIPaymentReversalActivity::class.java).apply {
                putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
                putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
            }

            is Terminal.SPOS -> SPOSIntentFactory.createPaymentReversalIntent(input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): TerminalOperationStatus {
        return if (resultCode == Activity.RESULT_OK) {
            if (intent?.extras?.containsKey(ExtraKeys.EXTRAS_RESULT) == true) {
                intent.extras?.let {
                    BundleCompat.getParcelable(
                        it,
                        ExtraKeys.EXTRAS_RESULT,
                        TerminalOperationStatus::class.java
                    )
                } ?: TerminalOperationStatus.Canceled
            } else {
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent)
            }
        } else {
            if (intent?.extras?.containsKey(SPOSKey.ResultExtra.ERROR) == true) {
                SPOSResponseHandler.handleTransactionResponse(resultCode, intent)
            } else {
                TerminalOperationStatus.Canceled
            }
        }
    }
}

class ReversalRequest(
    val config: Terminal,
    val transactionId: String,
    val amount: BigDecimal,
    val tip: BigDecimal = BigDecimal.ZERO,
    val currency: ISOAlphaCurrency,
    val receiptNo: String
) {
    override fun toString() = "ReversalRequest(" +
            "config=$config, " +
            "transactionId=$transactionId, " +
            "amount=$amount, " +
            "tip=$tip, " +
            "currency=$currency" +
            "receiptNo=$receiptNo, " +
            ")"

    override fun equals(other: Any?) = other is ReversalRequest &&
            config == other.config &&
            transactionId == other.transactionId &&
            amount == other.amount &&
            tip == other.tip &&
            currency == other.currency &&
            receiptNo == other.receiptNo

    override fun hashCode() = Objects.hash(config, transactionId, amount, tip, currency, receiptNo)
}