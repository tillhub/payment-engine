package de.tillhub.paymentengine.opi.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.viewBinding
import de.tillhub.paymentengine.opi.OPIService
import de.tillhub.paymentengine.opi.data.OPITerminal
import java.math.BigDecimal

internal class OPIPartialRefundActivity : OPITerminalActivity() {

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
            opiService.abortRequest()
            finish()
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

    override fun showOperationErrorStatus(status: String) {
        binding.message.text = status
    }

    override fun startOperation() {
        opiService.startPartialRefund(amount, currency)
    }

    override fun showCancel() {
        binding.buttonCancel.isVisible = true
    }

    override fun finishWithSuccess(state: OPIService.State.ResultSuccess) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    OPITerminal.TYPE
                )
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Refund.Success(state.data)
                )
            }
        )
        finish()
    }

    override fun finishWithError(state: OPIService.State.ResultError) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    OPITerminal.TYPE
                )
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Refund.Error(state.data)
                )
            }
        )
        finish()
    }

    companion object {
        private const val TAG = "OPIPaymentActivity"
    }
}