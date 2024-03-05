package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Random
import java.util.UUID

/**
 * This is called to start of a terminal reconciliation,
 * it sets up the manager so the data from the operation is collected correctly.
 */
interface ReconciliationManager : CardManager {
    fun startReconciliation()
    fun startReconciliation(configName: String)
    fun startReconciliation(config: Terminal)
}

class ReconciliationManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    private val registry: ActivityResultRegistry,
    private val defaultConfig: Terminal = Terminal.ZVT()
) : CardManagerImpl(configs, terminalState), ReconciliationManager {

    private lateinit var reconciliationContract: ActivityResultLauncher<Terminal>

    private val activityResultKey: String by lazy {
        "reconciliation_" + UUID.randomUUID().toString() + "_rq#" + Random().nextInt()
    }

    override fun registerResultRegistry(owner: LifecycleOwner) {
        reconciliationContract = registry.register(
            activityResultKey, owner, TerminalReconciliationContract()
        ) { result ->
            terminalState.tryEmit(result)
        }
    }

    override fun startReconciliation() {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startReconciliation(configName)
    }

    override fun startReconciliation(configName: String) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startReconciliation(terminalConfig)
    }

    override fun startReconciliation(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Reconciliation)
        reconciliationContract.launch(config)
    }
}