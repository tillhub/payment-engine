package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.opi.data.OpiTerminal
import de.tillhub.paymentengine.zvt.data.ZvtTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

class ReversalManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var transactionState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var reversalContract: ActivityResultLauncher<ReversalRequest>

    lateinit var target: ReversalManager

    beforeEach {
        configs = mutableMapOf()
        transactionState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))
        resultCaller = mockk(relaxed = true)
        reversalContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(PaymentReversalContract::class), any())
        } returns reversalContract

        target = ReversalManagerImpl(
            configs = configs,
            terminalState = transactionState,
            resultCaller = resultCaller,
            reversalContract = reversalContract
        )
    }

    test("startReversalTransaction should use default config when no configId provided") {
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")
        val receiptNo = "R12345"
        val tip = BigDecimal.ZERO

        target.startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            receiptNo = receiptNo
        )

        verify {
            reversalContract.launch(
                ReversalRequest(
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency,
                    tip = tip,
                    config = ZvtTerminal.create(),
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value shouldBe TerminalOperationStatus.Reversal.Pending(receiptNo)
    }

    test("startReversalTransaction with configId should launch reversal contract") {
        val terminal = OpiTerminal.create()
        configs["opi"] = terminal
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")
        val receiptNo = "R12345"
        val tip = BigDecimal.ZERO

        target.startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            configId = "opi",
            receiptNo = receiptNo
        )

        verify {
            reversalContract.launch(
                ReversalRequest(
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency,
                    tip = tip,
                    config = terminal,
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value shouldBe TerminalOperationStatus.Reversal.Pending(receiptNo)
    }

    test("startReversalTransaction custom Terminal should launch reversal contract") {
        val transactionId = "12345"
        val amount = BigDecimal(100)
        val currency = ISOAlphaCurrency("EUR")
        val receiptNo = "R12345"
        val tip = BigDecimal.ZERO

        target.startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            config = OpiTerminal.create(),
            receiptNo = receiptNo
        )

        verify {
            reversalContract.launch(
                ReversalRequest(
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency,
                    tip = tip,
                    config = OpiTerminal.create(),
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value shouldBe TerminalOperationStatus.Reversal.Pending(receiptNo)
    }
})
