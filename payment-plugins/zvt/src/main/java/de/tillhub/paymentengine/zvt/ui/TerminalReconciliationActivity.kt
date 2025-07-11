package de.tillhub.paymentengine.zvt.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.databinding.ActivityTerminalReconciliationBinding
import de.tillhub.paymentengine.zvt.helper.viewBinding
import de.tillhub.paymentengine.zvt.data.ZvtTerminal

internal class TerminalReconciliationActivity : CardTerminalActivity() {

    private val binding by viewBinding(ActivityTerminalReconciliationBinding::inflate)

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
        doReconciliation()
    }

    override fun setCancelVisibility(visible: Boolean) {
        binding.buttonCancel.isVisible = visible
    }

    override fun finishWithError(state: CardTerminalViewModel.State.Error) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZvtTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.reconciliation)
            }
        )
        finish()
    }

    override fun finishWithSuccess(state: CardTerminalViewModel.State.Success) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZvtTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.reconciliation)
            }
        )
        finish()
    }
}
