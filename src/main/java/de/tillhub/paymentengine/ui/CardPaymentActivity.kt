package de.tillhub.paymentengine.ui

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding

@AndroidEntryPoint
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
            (cardPaymentManager.transactionState as? LavegoTerminalOperation.Pending.Payment)?.let { payment ->
                Toast.makeText(applicationContext, "Initiate payment", Toast.LENGTH_SHORT).show()
                doPayment(
                    paymentAmount = payment.amount,
                    currency = payment.currency
                )
            } ?: finish()
        }
    }
}
