package de.tillhub.paymentengine.opi.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.viewBinding

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

    companion object {
        private const val TAG = "OPIPaymentReversalActivity"
    }
}