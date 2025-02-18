package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRecoveryContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
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
        configs = mutableMapOf("spos" to Terminal.SPOS(id = "spos"))
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

    test("startSPOSRecovery by default terminal") {
        target.startRecovery()

        verify {
            recoveryContract.launch(Terminal.SPOS(id = "spos"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Recovery
    }

    test("startSPOSRecovery by config name") {
        configs["spos2"] = Terminal.SPOS(id = "spos2")
        target.startRecovery("spos2")

        verify {
            recoveryContract.launch(Terminal.SPOS(id = "spos2"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Recovery
    }

    test("startSPOSRecovery by terminal") {
        target.startRecovery(Terminal.SPOS())

        verify {
            recoveryContract.launch(Terminal.SPOS())
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Recovery
    }

    test("contract failing to launch recovery request due to no activity") {
        every { recoveryContract.launch(any()) } answers {
            throw ActivityNotFoundException("Spos not installed")
        }

        target.startRecovery(Terminal.SPOS())

        val result = terminalState.first()

        result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
        result.resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})