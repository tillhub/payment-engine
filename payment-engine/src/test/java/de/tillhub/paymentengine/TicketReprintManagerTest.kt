package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TicketReprintContract
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

class TicketReprintManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var ticketReprintContract: ActivityResultLauncher<Terminal>

    lateinit var target: TicketReprintManager

    beforeTest {
        configs = mutableMapOf("spos" to Terminal.SPOS(id = "spos"))
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
            ticketReprintContract.launch(Terminal.SPOS(id = "spos"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.TicketReprint
    }

    test("startTicketReprint by config name") {
        configs["spos2"] = Terminal.SPOS(id = "spos2")
        target.startTicketReprint("spos2")

        verify {
            ticketReprintContract.launch(Terminal.SPOS(id = "spos2"))
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.TicketReprint
    }

    test("startTicketReprint by terminal") {
        target.startTicketReprint(Terminal.SPOS())

        verify {
            ticketReprintContract.launch(Terminal.SPOS())
        }

        terminalState.value shouldBe TerminalOperationStatus.Pending.TicketReprint
    }

    test("contract failing to launch recovery request due to no activity") {
        every { ticketReprintContract.launch(any()) } answers {
            throw ActivityNotFoundException("Spos not installed")
        }

        target.startTicketReprint(Terminal.SPOS())

        val result = terminalState.first()

        result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
        result.resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})