package de.tillhub.paymentengine.zvt.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.lavego.sdk.Payment
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.viewBinding
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import java.math.BigDecimal

internal class CardPaymentActivity : CardTerminalActivity() {

    companion object {
        private const val TAG = "CardPaymentActivity"
    }

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    private val amount: BigDecimal by lazy {
        intent.extras?.let {
            BundleCompat.getSerializable(it, ExtraKeys.EXTRA_AMOUNT, BigDecimal::class.java)
                ?: throw IllegalArgumentException("$TAG: Argument amount is missing")
        } ?: throw IllegalArgumentException("$TAG: Extras are missing")
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

        binding.buttonCancel.setOnClickListener {
            doAbortOperation()
        }
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

    override fun setCancelVisibility(visible: Boolean) {
        binding.buttonCancel.isVisible = visible
    }

    override fun finishWithSuccess(state: CardTerminalViewModel.State.Success) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZvtTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.payment)
            }
        )
        finish()
    }

    override fun finishWithError(state: CardTerminalViewModel.State.Error) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZvtTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.payment)
            }
        )
        finish()
    }
}
