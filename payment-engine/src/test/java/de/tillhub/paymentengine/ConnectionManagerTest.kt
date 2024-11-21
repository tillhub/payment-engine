package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

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

    test("startSPOSConnect by default terminal ") {
        target.startSPOSConnect()

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting)
            connectContract.launch(Terminal.ZVT())
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting
    }

    test("startSPOSConnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startSPOSConnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting)
            connectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting
    }

    test("startSPOSConnect by terminal") {
        val terminal = Terminal.SPOS()
        target.startSPOSConnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting)
            connectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting
    }

    test("startSPOSDisconnect by default terminal ") {
        target.startSPOSDisconnect()

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting)
            disconnectContract.launch(Terminal.ZVT())
        }
        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting
    }

    test("startSPOSDisconnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startSPOSDisconnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting
    }

    test("startSPOSDisconnect by terminal") {
        val terminal = Terminal.SPOS()
        target.startSPOSDisconnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting
    }
})
