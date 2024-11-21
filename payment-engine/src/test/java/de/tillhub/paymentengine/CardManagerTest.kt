package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class CardManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var target: CardManagerImpl

    beforeAny {
        configs = mockk(relaxed = true)
        terminalState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))

        target = object : CardManagerImpl(configs, terminalState) {}
    }

    test("putTerminalConfig should add terminal to the configs map") {
        val terminal = Terminal.OPI()
        target.putTerminalConfig(terminal)
        verify { configs[terminal.name] = terminal }
    }

    test("observePaymentState should return the terminal state flow") {
        val result = target.observePaymentState()
        result shouldBe terminalState
    }

    test("defaultConfig should return default Terminal configuration") {
        val defaultTerminal = target.defaultConfig
        defaultTerminal shouldBe Terminal.ZVT()
    }
})
