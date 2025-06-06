package de.tillhub.paymentengine.zvt.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.databinding.ActivityCardPaymentBinding
import de.tillhub.paymentengine.helper.viewBinding
import de.tillhub.paymentengine.zvt.data.ZVTTerminal

internal class TerminalLoginActivity : CardTerminalActivity() {
    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

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

    override fun startOperation() = Unit

    override fun setCancelVisibility(visible: Boolean) {
        binding.buttonCancel.isVisible = visible
    }

    override fun finishWithError(state: CardTerminalViewModel.State.Error) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZVTTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.connect)
            }
        )
        finish()
    }

    override fun finishWithSuccess(state: CardTerminalViewModel.State.Success) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    ZVTTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, state.connect)
            }
        )
        finish()
    }

    override fun onCompletion(completion: String) {
        viewModel.setupFinished(completion, ::moveAppToFront)
    }
}
