package de.tillhub.paymentengine.zvt.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.databinding.ActivityTerminalReconciliationBinding
import de.tillhub.paymentengine.helper.viewBinding

internal class TerminalReconciliationActivity : CardTerminalActivity() {

    private val binding by viewBinding(ActivityTerminalReconciliationBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonCancel.setOnClickListener {
            doAbortOperation()
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

    override fun startOperation() {
        analytics?.logOperation("Operation: RECONCILIATION\n$config")

        doReconciliation()
    }

    override fun showCancel() {
        binding.buttonCancel.isVisible = true
    }
}
