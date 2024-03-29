package de.tillhub.paymentengine.ui

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.lavego.sdk.Payment
import de.tillhub.paymentengine.contract.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.serializable
import java.math.BigDecimal

class CardPaymentActivity : CardTerminalActivity() {

    companion object {
        private const val TAG = "CardPaymentActivity"
    }

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    private val amount: BigDecimal by lazy {
        intent.extras?.serializable<BigDecimal>(ExtraKeys.EXTRA_AMOUNT)
            ?: throw IllegalArgumentException("$TAG: Argument amount is missing")
    }
    private val currency: ISOAlphaCurrency by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency::class.java)
                ?: throw IllegalArgumentException("$TAG: Argument currency is missing")
        } ?: throw IllegalArgumentException("$TAG: Extras are missing")
    }

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
        doPayment(Payment(amount, currency.value))
    }
}
