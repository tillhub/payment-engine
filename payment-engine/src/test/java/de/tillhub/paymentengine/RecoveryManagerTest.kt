package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRecoveryContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.testing.TestTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class RecoveryManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var recoveryContract: ActivityResultLauncher<Terminal>

    lateinit var target: RecoveryManager

    beforeTest {
        configs = mutableMapOf("external_terminal" to TestTerminal("external_terminal"))
        terminalState = MutableStateFlow(TerminalOperationStatus.Waiting)
        resultCaller = mockk(relaxed = true)
        recoveryContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(PaymentRecoveryContract::class), any())
        } returns recoveryContract

        target = RecoveryManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller,
            recoveryContract = recoveryContract
        )
    }

    test("startRecovery by default terminal") {
        target.startRecovery()

        verify {
            recoveryContract.launch(TestTerminal("external_terminal"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Recovery.Pending
    }

    test("startRecovery by config name") {
        configs["external_terminal2"] = TestTerminal("external_terminal2")
        target.startRecovery("external_terminal2")

        verify {
            recoveryContract.launch(TestTerminal("external_terminal2"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Recovery.Pending
    }

    test("startRecovery by terminal") {
        target.startRecovery(TestTerminal("external_terminal"))

        verify {
            recoveryContract.launch(TestTerminal("external_terminal"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Recovery.Pending
    }

    test("startTicketReprint by OPI terminal should throw error") {
        every { recoveryContract.launch(any()) } answers {
            throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
        }
        val terminal = TestTerminal("test")

        target.startRecovery(terminal)

        val result = terminalState.first()

        verify {
            recoveryContract.launch(terminal)
        }

        result.shouldBeInstanceOf<TerminalOperationStatus.Recovery.Error>()
        result.response.resultCode shouldBe ResultCodeSets.ACTION_NOT_SUPPORTED
    }
})