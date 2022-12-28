package de.tillhub.paymentengine.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.TerminalManager.Companion.EXTRA_PAYMENT_AMOUNT
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding

class CardPaymentActivity : TerminalActivity() {

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    private lateinit var amount: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        amount = intent.getStringExtra(EXTRA_PAYMENT_AMOUNT)
            ?: throw IllegalArgumentException("Missing argument: $EXTRA_PAYMENT_AMOUNT")
    }

    override fun showIntermediateStatus(status: String) {
        binding.message.text = status
    }

    override fun showInstructions() {
        binding.instructions.isVisible = true
        binding.loader.isGone = true
    }

    override fun startOperation() {
        viewModel.doPayment(amount)
    }
}