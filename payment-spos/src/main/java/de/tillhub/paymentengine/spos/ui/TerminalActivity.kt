package de.tillhub.paymentengine.spos.ui

import android.app.Activity
import android.content.ActivityNotFoundException
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
import de.tillhub.paymentengine.spos.SPOSResponseHandler.getErrorAppNotFound
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentRecoveryContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentRefundContract
import de.tillhub.paymentengine.spos.contracts.SPOSPaymentReversalContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalConnectContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalDisconnectContract
import de.tillhub.paymentengine.spos.contracts.SPOSTerminalReconciliationContract
import de.tillhub.paymentengine.spos.contracts.SPOSTicketReprintContract
import de.tillhub.paymentengine.spos.data.SPOSExtraKeys
import de.tillhub.paymentengine.spos.SPOSRequestBuilder
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import de.tillhub.paymentengine.spos.databinding.ActivityTerminalBinding
import kotlin.reflect.KClass

internal class TerminalActivity : AppCompatActivity() {

    private val binding by lazy { ActivityTerminalBinding.inflate(layoutInflater) }

    private val paymentContract: ActivityResultLauncher<PaymentRequest> by lazy {
        registerForActivityResult(SPOSPaymentContract(), ::handleResult)
    }

    private val paymentRecoveryContract: ActivityResultLauncher<Terminal> by lazy {
        registerForActivityResult(SPOSPaymentRecoveryContract(), ::handleResult)
    }

    private val paymentRefundContract: ActivityResultLauncher<RefundRequest> by lazy {
        registerForActivityResult(SPOSPaymentRefundContract(), ::handleResult)
    }

    private val paymentReversalContract: ActivityResultLauncher<ReversalRequest> by lazy {
        registerForActivityResult(SPOSPaymentReversalContract(), ::handleResult)
    }

    private val connectContract: ActivityResultLauncher<SPOSTerminal> by lazy {
        registerForActivityResult(SPOSTerminalConnectContract(), ::handleResult)
    }

    private val disconnectContract: ActivityResultLauncher<SPOSTerminal> by lazy {
        registerForActivityResult(SPOSTerminalDisconnectContract(), ::handleResult)
    }

    private val reprintContract: ActivityResultLauncher<Terminal> by lazy {
        registerForActivityResult(SPOSTicketReprintContract(), ::handleResult)
    }

    private val reconciliationContract: ActivityResultLauncher<Terminal> by lazy {
        registerForActivityResult(SPOSTerminalReconciliationContract(), ::handleResult)
    }

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
            SPOSExtraKeys.ACTION_CONNECT -> startOperation(
                launcher = connectContract,
                data = config,
                errClass = TerminalOperationStatus.Login::class
            )
            SPOSExtraKeys.ACTION_PAYMENT -> startOperation(
                launcher = paymentContract,
                data = SPOSRequestBuilder.buildPaymentRequest(intent),
                errClass = TerminalOperationStatus.Payment::class
            )
            SPOSExtraKeys.ACTION_REVERSAL -> startOperation(
                launcher = paymentReversalContract,
                data = SPOSRequestBuilder.buildReversalRequest(intent),
                errClass = TerminalOperationStatus.Reversal::class
            )
            SPOSExtraKeys.ACTION_REFUND -> startOperation(
                launcher = paymentRefundContract,
                data = SPOSRequestBuilder.buildRefundRequest(intent),
                errClass = TerminalOperationStatus.Refund::class
            )
            SPOSExtraKeys.ACTION_RECONCILIATION -> startOperation(
                launcher = reconciliationContract,
                data = config,
                errClass = TerminalOperationStatus.Reconciliation::class
            )
            SPOSExtraKeys.ACTION_RECOVERY -> startOperation(
                launcher = paymentRecoveryContract,
                data = config,
                errClass = TerminalOperationStatus.Recovery::class
            )
            SPOSExtraKeys.ACTION_DISCONNECT -> startOperation(
                launcher = disconnectContract,
                data = config,
                errClass = TerminalOperationStatus.Login::class
            )
            SPOSExtraKeys.ACTION_REPRINT -> startOperation(
                launcher = reprintContract,
                data = config,
                errClass = TerminalOperationStatus.TicketReprint::class
            )
            else -> throw IllegalArgumentException("TerminalActivity: Argument action is invalid")
        }
    }

    private fun<T, U : TerminalOperationStatus> startOperation(
        launcher: ActivityResultLauncher<T>,
        data: T,
        errClass: KClass<U>
    ) {
        try {
            launcher.launch(data)
        } catch (_: ActivityNotFoundException) {
            handleResult(errClass.getErrorAppNotFound())
        }
    }

    private fun handleResult(result: TerminalOperationStatus) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ExtraKeys.EXTRAS_PROTOCOL,
                    SPOSTerminal.TYPE
                )
                putExtra(ExtraKeys.EXTRAS_RESULT, result)
            }
        )
        finish()
    }
}