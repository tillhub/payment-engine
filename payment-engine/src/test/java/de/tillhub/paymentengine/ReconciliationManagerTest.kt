package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

        verify { reconciliationContract.launch(Terminal.ZVT()) }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Reconciliation
    }

    test("startReconciliation with configId should and launch reconciliation contract") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal

        target.startReconciliation(configId = "opi")

        verify { reconciliationContract.launch(terminal) }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Reconciliation
    }

    test("startReconciliation with custom Terminal should and launch reconciliation contract") {
        val customTerminal = Terminal.SPOS()

        target.startReconciliation(customTerminal)

        verify { reconciliationContract.launch(customTerminal) }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Reconciliation
    }

    test("contract failing to launch request due to no activity") {
        every { reconciliationContract.launch(any()) } answers {
            throw ActivityNotFoundException()
        }

        val customTerminal = Terminal.SPOS()

        target.startReconciliation(customTerminal)

        verify { reconciliationContract.launch(customTerminal) }

        terminalState.value.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
        (terminalState.value as TerminalOperationStatus.Error.SPOS)
            .resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})
