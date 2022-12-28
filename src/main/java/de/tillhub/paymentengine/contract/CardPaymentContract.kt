package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.paymentengine.TerminalManager.Companion.EXTRA_CARD_PAYMENT_CONFIG
import de.tillhub.paymentengine.TerminalManager.Companion.EXTRA_CARD_SALE_CONFIG
import de.tillhub.paymentengine.TerminalManager.Companion.EXTRA_PAYMENT_AMOUNT
import de.tillhub.paymentengine.TerminalManager.Companion.RESULT_DATA
import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import de.tillhub.paymentengine.data.Payment
import de.tillhub.paymentengine.ui.CardPaymentActivity
import java.math.BigDecimal

class CardPaymentContract : ActivityResultContract<CardPaymentContract.Input, Payment<LavegoTerminalOperation>>() {
    override fun createIntent(context: Context, input: Input) =
        Intent(context, CardPaymentActivity::class.java).apply {
            putExtra(EXTRA_CARD_PAYMENT_CONFIG, input.cardPaymentConfig)
            putExtra(EXTRA_CARD_SALE_CONFIG, input.cardSaleConfig)
            putExtra(EXTRA_PAYMENT_AMOUNT, input.amount.toString())
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Payment<LavegoTerminalOperation> {
        if (resultCode != Activity.RESULT_OK) {
            return Payment.Error("Result code not ok. Result: $resultCode")
        }
        return intent?.extras?.let {
            Payment.Success(it.getParcelable<LavegoTerminalOperation>(RESULT_DATA) as LavegoTerminalOperation)
        } ?: Payment.Error("Extras data missing")
    }

    data class Input(
        val cardPaymentConfig: CardPaymentConfig,
        val cardSaleConfig: CardSaleConfig,
        val amount: BigDecimal
    )
}