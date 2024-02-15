package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.PaymentRefundContract
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.math.BigDecimal

class CardPaymentManagerImpl(private val registry: ActivityResultRegistry) : CardPaymentManager {

    private val configs: MutableMap<String, Terminal> = mutableMapOf()
    private val transactionState = MutableSharedFlow<TerminalOperationStatus>(extraBufferCapacity = 1)

    override fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    override fun getOrDefault(configName: String): Terminal {
        return configs.getOrDefault(configName, Terminal.ZVT())
    }

    override fun observePaymentState(): SharedFlow<TerminalOperationStatus> = transactionState

    private lateinit var paymentResultContract: ActivityResultLauncher<PaymentRequest>
    private lateinit var refundContract: ActivityResultLauncher<RefundRequest>
    private lateinit var reversalContract: ActivityResultLauncher<ReversalRequest>
    private lateinit var reconciliationContract: ActivityResultLauncher<Terminal>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        paymentResultContract = registry.register(
            PaymentResultContract.REGISTER_KEY, owner, PaymentResultContract()
        ) { result ->
            transactionState.tryEmit(result)
        }
        refundContract = registry.register(
            PaymentRefundContract.REGISTER_KEY, owner, PaymentRefundContract()
        ) { result ->
            transactionState.tryEmit(result)
        }
        reversalContract = registry.register(
            PaymentReversalContract.REGISTER_KEY, owner, PaymentReversalContract()
        ) { result ->
            transactionState.tryEmit(result)
        }
        reconciliationContract = registry.register(
            TerminalReconciliationContract.REGISTER_KEY, owner, TerminalReconciliationContract()
        ) { result ->
            transactionState.tryEmit(result)
        }
    }

    override fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startPaymentTransaction(amount, currency, configName)
    }

    override fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String) {
        val terminalConfig = configs.getOrDefault(configName, Terminal.ZVT())
        transactionState.tryEmit(TerminalOperationStatus.Pending.Payment(amount, currency))
        paymentResultContract.launch(
            PaymentRequest(terminalConfig, amount, currency)
        )
    }

    override fun startReversalTransaction(receiptNo: String) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startReversalTransaction(receiptNo, configName)
    }

    override fun startReversalTransaction(receiptNo: String, configName: String) {
        val terminalConfig = configs.getOrDefault(configName, Terminal.ZVT())
        transactionState.tryEmit(TerminalOperationStatus.Pending.Reversal(receiptNo))
        reversalContract.launch(
            ReversalRequest(terminalConfig, receiptNo)
        )
    }

    override fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startRefundTransaction(amount, currency, configName)
    }

    override fun startRefundTransaction(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        configName: String
    ) {
        val terminalConfig = configs.getOrDefault(configName, Terminal.ZVT())
        transactionState.tryEmit(TerminalOperationStatus.Pending.Refund(amount, currency))
        refundContract.launch(
            RefundRequest(terminalConfig, amount, currency)
        )
    }

    override fun startReconciliation() {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startReconciliation(configName)
    }

    override fun startReconciliation(configName: String) {
        val terminalConfig = configs.getOrDefault(configName, Terminal.ZVT())
        transactionState.tryEmit(TerminalOperationStatus.Pending.Reconciliation)
        reconciliationContract.launch(terminalConfig)
    }
}