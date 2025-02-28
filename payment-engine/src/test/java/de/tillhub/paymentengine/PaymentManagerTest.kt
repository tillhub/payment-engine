package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class PaymentManagerTest : FunSpec({

    lateinit var configs: MutableMap<String, Terminal>
    lateinit var terminalState: MutableStateFlow<TerminalOperationStatus>
    lateinit var resultCaller: ActivityResultCaller
    lateinit var paymentResultContract: ActivityResultLauncher<PaymentRequest>

    lateinit var target: PaymentManager

    beforeEach {
        configs = mutableMapOf()
        terminalState = spyk(MutableStateFlow(TerminalOperationStatus.Waiting))
        resultCaller = mockk(relaxed = true)
        paymentResultContract = mockk(relaxed = true)

        every {
            resultCaller.registerForActivityResult(ofType(PaymentResultContract::class), any())
        } returns paymentResultContract

        target = PaymentManagerImpl(
            configs = configs,
            terminalState = terminalState,
            resultCaller = resultCaller,
            paymentResultContract = paymentResultContract
        )
    }

    test("startPaymentTransaction should use default config when no configId provided") {
        val transactionId = "tx789"
        val amount = BigDecimal(300)
        val tip = BigDecimal(30)
        val currency = ISOAlphaCurrency("EUR")

        target.startPaymentTransaction(transactionId, amount, tip, currency)

        verify {
            paymentResultContract.launch(
                match {
                    it.transactionId == transactionId &&
                            it.amount == amount &&
                            it.tip == tip &&
                            it.currency == currency &&
                            it.config == Terminal.ZVT()
                }
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Payment.Pending(amount, currency)
    }

    test("startPaymentTransaction with configId should launch payment result contract") {
        val transactionId = "tx456"
        val amount = BigDecimal(200)
        val tip = BigDecimal(20)
        val currency = ISOAlphaCurrency("EUR")
        val terminal = Terminal.OPI()
        configs["opi"] = terminal

        target.startPaymentTransaction(transactionId, amount, tip, currency, "opi")

        verify {
            paymentResultContract.launch(
                match {
                    it.transactionId == transactionId &&
                            it.amount == amount &&
                            it.tip == tip &&
                            it.currency == currency &&
                            it.config == terminal
                }
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Payment.Pending(amount, currency)
    }

    test("startPaymentTransaction with Terminal should launch payment result contract") {
        val transactionId = "tx123"
        val amount = BigDecimal(100)
        val tip = BigDecimal(10)
        val currency = ISOAlphaCurrency("EUR")
        val terminal = Terminal.SPOS()

        target.startPaymentTransaction(transactionId, amount, tip, currency, terminal)

        verify {
            paymentResultContract.launch(
                match {
                    it.transactionId == transactionId &&
                            it.amount == amount &&
                            it.tip == tip &&
                            it.currency == currency &&
                            it.config == terminal
                }
            )
        }

        terminalState.value shouldBe TerminalOperationStatus.Payment.Pending(amount, currency)
    }

    test("contract failing to launch request due to no activity") {
        every { paymentResultContract.launch(any()) } answers {
            throw ActivityNotFoundException()
        }

        val transactionId = "tx123"
        val amount = BigDecimal(100)
        val tip = BigDecimal(10)
        val currency = ISOAlphaCurrency("EUR")
        val terminal = Terminal.SPOS()

        target.startPaymentTransaction(transactionId, amount, tip, currency, terminal)

        verify {
            paymentResultContract.launch(
                match {
                    it.transactionId == transactionId &&
                            it.amount == amount &&
                            it.tip == tip &&
                            it.currency == currency &&
                            it.config == terminal
                }
            )
        }

        terminalState.value.shouldBeInstanceOf<TerminalOperationStatus.Payment.Error>()
        (terminalState.value as TerminalOperationStatus.Payment.Error).response
            .resultCode shouldBe ResultCodeSets.APP_NOT_FOUND_ERROR
    }
})
