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
import de.tillhub.paymentengine.opi.data.OpiTerminal

internal class OPILoginActivity : OPITerminalActivity() {

    private val binding by viewBinding(ActivityCardPaymentBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun showLoader() {
        binding.loader.isVisible = true
        binding.instructions.isGone = true

        binding.buttonCancel.setOnClickListener {
            opiService.abortRequest()
            finish()
        }
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

    override fun showCancel() {
        binding.buttonCancel.isVisible = true
    }

    override fun finishWithSuccess(state: OPIService.State.ResultSuccess) {
         setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    OpiTerminal.TYPE
                )
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Login.Connected(
                        date = state.data.date,
                        rawData = state.data.rawData,
                        terminalType = OpiTerminal.TYPE,
                        terminalId = state.data.data?.terminalId.orEmpty()
                    )
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
                    OpiTerminal.TYPE
                )
                putExtra(
                    ExtraKeys.EXTRAS_RESULT,
                    TerminalOperationStatus.Login.Error(
                        date = state.data.date,
                        rawData = state.data.rawData,
                        resultCode = state.data.resultCode
                    )
                )
            }
        )
        finish()
    }

    override fun startOperation() {
        opiService.startLogin()
    }
}