package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.data.OpiTerminal
import de.tillhub.paymentengine.testing.TestExternalTerminal
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@ExperimentalCoroutinesApi
class ConnectionManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var connectContract: ActivityResultLauncher<Terminal>
    lateinit var disconnectContract: ActivityResultLauncher<Terminal>

    lateinit var target: ConnectionManager

    beforeAny {
        configs = mutableMapOf()
        terminalState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))
        resultCaller = mockk(relaxed = true)
        connectContract = mockk(relaxed = true)
        disconnectContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(TerminalConnectContract::class), any())
        } returns connectContract
        every {
            resultCaller.registerForActivityResult(any<TerminalDisconnectContract>(), any())
        } returns disconnectContract

        target = ConnectionManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller,
            connectContract = connectContract,
            disconnectContract = disconnectContract
        )
    }

    test("startConnect by default terminal ") {
        target.startConnect()

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            connectContract.launch(ZvtTerminal.create())
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startConnect by config name") {
        val terminal = OpiTerminal.create()
        configs["opi"] = terminal
        target.startConnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            connectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startConnect by terminal") {
        val terminal = TestExternalTerminal("external_terminal")
        target.startConnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            connectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by default terminal ") {
        target.startSPOSDisconnect()

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            disconnectContract.launch(ZvtTerminal.create())
        }
        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by config name") {
        val terminal = OpiTerminal.create()
        configs["opi"] = terminal
        target.startSPOSDisconnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by terminal") {
        val terminal = TestExternalTerminal("external_terminal")
        target.startSPOSDisconnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by OPI terminal should throw error") {
        every { disconnectContract.launch(any()) } answers {
            throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
        }

        target.startSPOSDisconnect(OpiTerminal.create())

        val result = terminalState.first()

        verify {
            disconnectContract.launch(OpiTerminal.create())
        }

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()
        result.resultCode shouldBe ResultCodeSets.ACTION_NOT_SUPPORTED
    }
})
