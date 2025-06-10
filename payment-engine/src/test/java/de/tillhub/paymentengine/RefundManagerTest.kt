package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRefundContract
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.data.OpiTerminal
import de.tillhub.paymentengine.testing.TestExternalTerminal
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

class RefundManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var refundContract: ActivityResultLauncher<RefundRequest>

    lateinit var target: RefundManager

    beforeEach {
        configs = mutableMapOf()
        terminalState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))
        resultCaller = mockk(relaxed = true)
        refundContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(PaymentRefundContract::class), any())
        } returns refundContract

        target = RefundManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller
        )
    }

    test("startRefundTransaction should use default config when no configId provided") {
        val defaultTerminal = ZvtTerminal.create()
        configs.clear()
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")

        target.startRefundTransaction(
            transactionId = transactionId,
            amount = amount,
            currency = currency
        )

        verify {
            refundContract.launch(
                RefundRequest(
                    config = defaultTerminal,
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency
                )
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Refund.Pending(amount, currency)
    }

    test("startRefundTransaction with configId should launch refund contract") {
        val terminal = OpiTerminal.create()
        configs["opi"] = terminal
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")

        target.startRefundTransaction(
            transactionId = transactionId,
            amount = amount,
            currency = currency,
            configId = "opi"
        )

        verify {
            refundContract.launch(
                RefundRequest(
                    config = terminal,
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency
                )
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Refund.Pending(amount, currency)
    }

    test("startRefundTransaction with custom Terminal should launch refund contract") {
        val customTerminal = TestExternalTerminal("external_terminal")
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")

        target.startRefundTransaction(
            transactionId = transactionId,
            amount = amount,
            currency = currency,
            config = customTerminal
        )

        verify {
            refundContract.launch(
                RefundRequest(
                    config = customTerminal,
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency
                )
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Refund.Pending(amount, currency)
    }
})
