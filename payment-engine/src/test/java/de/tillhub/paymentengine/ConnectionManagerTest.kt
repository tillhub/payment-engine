package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
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
        target.startConnect()

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            connectContract.launch(Terminal.ZVT())
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSConnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startConnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            connectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSConnect by terminal") {
        val terminal = Terminal.SPOS()
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
            disconnectContract.launch(Terminal.ZVT())
        }
        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by config name") {
        val terminal = Terminal.OPI()
        configs["opi"] = terminal
        target.startSPOSDisconnect("opi")

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("startSPOSDisconnect by terminal") {
        val terminal = Terminal.SPOS()
        target.startSPOSDisconnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
            disconnectContract.launch(terminal)
        }

        terminalState.value shouldBe TerminalOperationStatus.Login.Pending
    }

    test("contract failing to launch disconnect request due to no activity") {
        every { disconnectContract.launch(any()) } answers {
            throw ActivityNotFoundException()
        }

        val terminal = Terminal.SPOS()
        target.startSPOSDisconnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            disconnectContract.launch(terminal)
        }

        terminalState.value.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()
        (terminalState.value as TerminalOperationStatus.Login.Error)
            .resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }

    test("contract failing to launch connect request due to no activity") {
        every { connectContract.launch(any()) } answers {
            throw ActivityNotFoundException()
        }

        val terminal = Terminal.SPOS()
        target.startConnect(terminal)

        verify(ordering = Ordering.ORDERED) {
            connectContract.launch(terminal)
        }

        terminalState.value.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()
        (terminalState.value as TerminalOperationStatus.Login.Error)
            .resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})
