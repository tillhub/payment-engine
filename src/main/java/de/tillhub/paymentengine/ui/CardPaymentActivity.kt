package de.tillhub.paymentengine.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding

class CardPaymentActivity : CardTerminalActivity() {

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    override fun showLoader() {
        binding.loader.isVisible = true
        binding.instructions.isGone = true
    }

    override fun showInstructions() {
        binding.instructions.isVisible = true
        binding.loader.isGone = true
    }

    override fun showIntermediateStatus(status: String) {
        binding.message.text = status
    }

    override fun startOperation() {
        if (cardPaymentManager.transactionState is LavegoTerminalOperation.Success ||
            cardPaymentManager.transactionState is LavegoTerminalOperation.Waiting) {
            finish()
        } else {
            (cardPaymentManager.transactionState as? LavegoTerminalOperation.Pending.Payment)?.amount?.let { amount ->
                doPayment(amount)
            } ?: finish()
        }
    }
}
