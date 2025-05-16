package de.tillhub.paymentengine.spos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentRecoveryContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentRefundContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentReversalContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalConnectContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalDisconnectContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalReconciliationContract
import de.tillhub.paymentengine.spos.contracts.SPOSTicketReprintContract
import de.tillhub.paymentengine.spos.data.SPOSExtraKeys
import de.tillhub.paymentengine.spos.data.SPOSRequestBuilder
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import de.tillhub.paymentengine.spos.databinding.ActivityTerminalBinding

internal class TerminalActivity : AppCompatActivity() {

    private val binding by lazy { ActivityTerminalBinding.inflate(layoutInflater) }

    private val paymentContract: ActivityResultLauncher<PaymentRequest> =
        registerForActivityResult(SPOSPaymentContract(), ::handleResult)

    private val paymentRecoveryContract: ActivityResultLauncher<Terminal> =
        registerForActivityResult(SPOSPaymentRecoveryContract(), ::handleResult)

    private val paymentRefundContract: ActivityResultLauncher<RefundRequest> =
        registerForActivityResult(SPOSPaymentRefundContract(), ::handleResult)

    private val paymentReversalContract: ActivityResultLauncher<ReversalRequest> =
        registerForActivityResult(SPOSPaymentReversalContract(), ::handleResult)

    private val connectContract: ActivityResultLauncher<SPOSTerminal> =
        registerForActivityResult(SPOSTerminalConnectContract(), ::handleResult)

    private val disconnectContract: ActivityResultLauncher<SPOSTerminal> =
        registerForActivityResult(SPOSTerminalDisconnectContract(), ::handleResult)

    private val reprintContract: ActivityResultLauncher<Terminal> =
        registerForActivityResult(SPOSTicketReprintContract(), ::handleResult)

    private val reconciliationContract: ActivityResultLauncher<Terminal> =
        registerForActivityResult(SPOSTerminalReconciliationContract(), ::handleResult)

    private val action: String by lazy {
        intent.getStringExtra(SPOSExtraKeys.EXTRA_ACTION)
            ?: throw IllegalArgumentException("TerminalActivity: Argument action is null")
    }

    private val config: SPOSTerminal by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CONFIG, SPOSTerminal::class.java)
                ?: throw IllegalArgumentException("TerminalActivity: Argument config is missing")
        } ?: throw IllegalArgumentException("TerminalActivity: Extras is null")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (action) {
            SPOSExtraKeys.ACTION_CONNECT -> startConnect()
            SPOSExtraKeys.ACTION_PAYMENT -> startPayment()
            SPOSExtraKeys.ACTION_REVERSAL -> startReversal()
            SPOSExtraKeys.ACTION_REFUND -> startRefund()
            SPOSExtraKeys.ACTION_RECONCILIATION -> startReconciliation()
            SPOSExtraKeys.ACTION_RECOVERY -> startRecovery()
            SPOSExtraKeys.ACTION_DISCONNECT -> startDisconnect()
            SPOSExtraKeys.ACTION_REPRINT -> startReprint()
            else -> throw IllegalArgumentException("TerminalActivity: Argument action is invalid")
        }
    }

    private fun startConnect() {
        connectContract.launch(config)
    }

    private fun startDisconnect() {
        disconnectContract.launch(config)
    }

    private fun startPayment() {
        paymentContract.launch(SPOSRequestBuilder.buildPaymentRequest(intent))
    }

    private fun startRefund() {
        paymentRefundContract.launch(SPOSRequestBuilder.buildRefundRequest(intent))
    }

    private fun startReversal() {
        paymentReversalContract.launch(SPOSRequestBuilder.buildReversalRequest(intent))
    }

    private fun startRecovery() {
        paymentRecoveryContract.launch(config)
    }

    private fun startReprint() {
        reprintContract.launch(config)
    }

    private fun startReconciliation() {
        reconciliationContract.launch(config)
    }

    private fun handleResult(result: TerminalOperationStatus) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(ExtraKeys.EXTRAS_RESULT, result) }
        )
        finish()
    }
}