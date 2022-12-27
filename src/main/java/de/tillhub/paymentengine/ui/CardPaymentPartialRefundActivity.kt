package de.tillhub.paymentengine.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.lavego.sdk.PaymentProtocol
import de.tillhub.paymentengine.data.LavegoTerminalOperation
import de.tillhub.paymentengine.databinding.ActivityCardPaymentPartialRefundBinding

class CardPaymentPartialRefundActivity : CardTerminalActivity() {

    private val binding by viewBinding(ActivityCardPaymentPartialRefundBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    override fun showLoader() {}

    override fun showInstructions() {}

    override fun showIntermediateStatus(status: String) {
        binding.statusMessage.text = status
    }

    override fun startOperation() {
        when (cardPaymentManager.getTransportConfiguration(cardPaymentConfig).paymentProtocol) {
            PaymentProtocol.Nexo -> {
                binding.instructions.isGone = true
                binding.nexoError.isVisible = true
            }
            PaymentProtocol.Zvt -> {
                if (cardPaymentManager.transactionState is LavegoTerminalOperation.Success ||
                    cardPaymentManager.transactionState is LavegoTerminalOperation.Waiting) {
                    finish()
                } else {
                    (cardPaymentManager.transactionState as? LavegoTerminalOperation.Pending.PartialRefund)?.let {
                        doPartialRefund(it.amount)
                    } ?: finish()
                }
            }
            null -> Unit
        }
    }
}
