package de.tillhub.paymentengine.opi.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.viewBinding
import de.tillhub.paymentengine.opi.OPIService
import de.tillhub.paymentengine.opi.data.OPITerminal

internal class OPIPaymentReversalActivity : OPITerminalActivity() {

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    private val stan: String by lazy {
        intent.getStringExtra(ExtraKeys.EXTRA_RECEIPT_NO)
            ?: throw IllegalArgumentException("$TAG: Argument receipt number is missing")
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
        opiService.startPaymentReversal(stan)
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
                    TerminalOperationStatus.Reversal.Success(state.data)
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
                    TerminalOperationStatus.Reversal.Error(state.data)
                )
            }
        )
        finish()
    }

    companion object {
        private const val TAG = "OPIPaymentReversalActivity"
    }
}