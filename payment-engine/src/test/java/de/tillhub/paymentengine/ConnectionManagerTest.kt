package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting) }
        verify { connectContract.launch(Terminal.ZVT()) }
    }

    test("startSPOSConnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startSPOSConnect("opi")

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting) }
        verify { connectContract.launch(terminal) }
    }

    test("startSPOSConnect by terminal") {
        val terminal = Terminal.SPOS()
        target.startSPOSConnect(terminal)

        terminalState.value shouldBe TerminalOperationStatus.Pending.Connecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting) }
        verify { connectContract.launch(terminal) }
    }

    test("startSPOSDisconnect by default terminal ") {
        target.startSPOSDisconnect()

        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting) }
        verify { disconnectContract.launch(Terminal.ZVT()) }
    }

    test("startSPOSDisconnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startSPOSDisconnect("opi")

        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting) }
        verify { disconnectContract.launch(terminal) }
    }

    test("startSPOSDisconnect by terminal") {
        val terminal = Terminal.SPOS()
        target.startSPOSDisconnect(terminal)

        terminalState.value shouldBe TerminalOperationStatus.Pending.Disconnecting

        verify { terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting) }
        verify { disconnectContract.launch(terminal) }
    }
})
