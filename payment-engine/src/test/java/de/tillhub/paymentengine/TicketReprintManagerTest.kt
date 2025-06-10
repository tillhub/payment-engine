package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TicketReprintContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.data.OpiTerminal
import de.tillhub.paymentengine.testing.TestTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class TicketReprintManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var ticketReprintContract: ActivityResultLauncher<Terminal>

    lateinit var target: TicketReprintManager

    beforeTest {
        configs = mutableMapOf("external_terminal" to TestTerminal("external_terminal"))
        terminalState = MutableStateFlow(TerminalOperationStatus.Waiting)
        resultCaller = mockk(relaxed = true)
        ticketReprintContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(TicketReprintContract::class), any())
        } returns ticketReprintContract

        target = TicketReprintManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller,
            ticketReprintContract = ticketReprintContract
        )
    }

    test("startTicketReprint by default terminal") {
        target.startTicketReprint()

        verify {
            ticketReprintContract.launch(TestTerminal("external_terminal"))
        }

        terminalState.value shouldBe TerminalOperationStatus.TicketReprint.Pending
    }

    test("startTicketReprint by config name") {
        configs["external_terminal2"] = TestTerminal("external_terminal2")
        target.startTicketReprint("external_terminal2")

        verify {
            ticketReprintContract.launch(TestTerminal("external_terminal2"))
        }

        terminalState.value shouldBe TerminalOperationStatus.TicketReprint.Pending
    }

    test("startTicketReprint by terminal") {
        target.startTicketReprint(TestTerminal("external_terminal"))

        verify {
            ticketReprintContract.launch(TestTerminal("external_terminal"))
        }

        terminalState.value shouldBe TerminalOperationStatus.TicketReprint.Pending
    }

    test("startTicketReprint by OPI terminal should throw error") {
        every { ticketReprintContract.launch(any()) } answers {
            throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
        }

        target.startTicketReprint(OpiTerminal.create())

        val result = terminalState.first()

        verify {
            ticketReprintContract.launch(OpiTerminal.create())
        }

        result.shouldBeInstanceOf<TerminalOperationStatus.TicketReprint.Error>()
        result.resultCode shouldBe ResultCodeSets.ACTION_NOT_SUPPORTED
    }
})