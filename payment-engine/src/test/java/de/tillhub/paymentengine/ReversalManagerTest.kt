package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
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

    test("startReversalTransaction should use default config when no configName provided") {
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
                    config = Terminal.ZVT(),
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value shouldBe TerminalOperationStatus.Pending.Reversal(receiptNo)
    }

    test("startReversalTransaction with configName should launch reversal contract") {
        val terminal = Terminal.OPI()
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
            configName = "opi",
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

        transactionState.value shouldBe TerminalOperationStatus.Pending.Reversal(receiptNo)
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
            config = Terminal.SPOS(),
            receiptNo = receiptNo
        )

        verify {
            reversalContract.launch(
                ReversalRequest(
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency,
                    tip = tip,
                    config = Terminal.SPOS(),
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value shouldBe TerminalOperationStatus.Pending.Reversal(receiptNo)
    }

    test("contract failing to launch request due to no activity") {
        every { reversalContract.launch(any()) } answers {
            throw ActivityNotFoundException()
        }

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
            config = Terminal.SPOS(),
            receiptNo = receiptNo
        )

        verify {
            reversalContract.launch(
                ReversalRequest(
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency,
                    tip = tip,
                    config = Terminal.SPOS(),
                    receiptNo = receiptNo
                )
            )
        }

        transactionState.value.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()
        (transactionState.value as TerminalOperationStatus.Error.SPOS)
            .resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})