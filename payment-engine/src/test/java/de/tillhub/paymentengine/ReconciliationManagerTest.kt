package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.data.OpiTerminal
import de.tillhub.paymentengine.testing.TestTerminal
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class ReconciliationManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var reconciliationContract: ActivityResultLauncher<Terminal>

    lateinit var target: ReconciliationManager

    beforeEach {
        configs = mutableMapOf()
        terminalState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))
        resultCaller = mockk(relaxed = true)
        reconciliationContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(TerminalReconciliationContract::class), any())
        } returns reconciliationContract

        target = ReconciliationManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller,
            reconciliationContract = reconciliationContract
        )
    }

    test("startReconciliation should use default config when no configId provided") {
        target.startReconciliation()

        verify { reconciliationContract.launch(ZvtTerminal.create()) }

        terminalState.value shouldBe TerminalOperationStatus.Reconciliation.Pending
    }

    test("startReconciliation with configId should and launch reconciliation contract") {
        val terminal = OpiTerminal.create()
        configs["opi"] = terminal

        target.startReconciliation(configId = "opi")

        verify { reconciliationContract.launch(terminal) }

        terminalState.value shouldBe TerminalOperationStatus.Reconciliation.Pending
    }

    test("startReconciliation with custom Terminal should and launch reconciliation contract") {
        val customTerminal = TestTerminal("external_terminal")

        target.startReconciliation(customTerminal)

        verify { reconciliationContract.launch(customTerminal) }

        terminalState.value shouldBe TerminalOperationStatus.Reconciliation.Pending
    }
})
