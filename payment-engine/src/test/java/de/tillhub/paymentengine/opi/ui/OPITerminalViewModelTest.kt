package de.tillhub.paymentengine.opi.ui

import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.opi.OPIChannelController
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.ui.OPITerminalViewModel.State
import de.tillhub.paymentengine.testing.ViewModelFunSpecs
import de.tillhub.paymentengine.testing.getOrAwaitValue
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

@ExperimentalCoroutinesApi
class OPITerminalViewModelTest : ViewModelFunSpecs({

    lateinit var terminal: Terminal.OPI
    lateinit var instant: Instant
    lateinit var throwable: Throwable

    lateinit var opiChannelController: OPIChannelController

    lateinit var target: OPITerminalViewModel

    val mutableOperationFlow = MutableStateFlow<OPIOperationStatus>(OPIOperationStatus.Idle)

    beforeTest {
        terminal = mockk()
        instant = mockk()
        throwable = mockk()

        opiChannelController = mockk {
            every { operationState } returns mutableOperationFlow
            every { init(any()) } just Runs
            every { close() } just Runs
            coEvery { login() } just Runs
            coEvery { initiateCardPayment(any(), ISOAlphaCurrency("EUR")) } just Runs
            coEvery { initiatePaymentReversal(any()) } just Runs
            coEvery { initiatePartialRefund(any(), ISOAlphaCurrency("EUR")) } just Runs
            coEvery { initiateReconciliation() } just Runs
        }

        target = OPITerminalViewModel(opiChannelController)
    }

    test("modifyAmountForOpi") {
        // https://en.wikipedia.org/wiki/ISO_4217 to check decimal places for currency

        // Icelandic krona has 0 decimal places
        val isk = target.modifyAmountForOpi(100.0.toBigDecimal(), ISOAlphaCurrency("ISK"))
        // Iraqi dinar has 3 decimal places
        val iqd = target.modifyAmountForOpi(100.0.toBigDecimal(), ISOAlphaCurrency("IQD"))
        // Euro has 2 decimal places
        val eur = target.modifyAmountForOpi(100.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

        isk shouldBe 100.0.toBigDecimal()
        iqd shouldBe 0.1.toBigDecimal().setScale(4)
        eur shouldBe 1.0.toBigDecimal().setScale(3)
    }

    test("init") {
        target.init(terminal)

        verify(Ordering.ORDERED) {
            opiChannelController.init(terminal)
            opiChannelController.operationState
        }
    }

    test("opiOperationState") {
        target.init(terminal)

        target.opiOperationState.getOrAwaitValue() shouldBe State.Idle

        mutableOperationFlow.value = OPIOperationStatus.Pending.Login
        target.opiOperationState.getOrAwaitValue() shouldBe State.Pending.Login

        mutableOperationFlow.value = OPIOperationStatus.Pending.Operation(
            date = instant,
            messageLines = emptyList()
        )
        target.opiOperationState.getOrAwaitValue() shouldBe State.Pending.NoMessage

        mutableOperationFlow.value = OPIOperationStatus.Pending.Operation(
            date = instant,
            messageLines = listOf(
                "line 1",
                "line 2",
                "line 3"
            )
        )
        target.opiOperationState.getOrAwaitValue() shouldBe State.Pending.WithMessage(
            "line 1\nline 2\nline 3\n"
        )

        mutableOperationFlow.value = OPIOperationStatus.LoggedIn
        target.opiOperationState.getOrAwaitValue() shouldBe State.LoggedIn

        mutableOperationFlow.value = OPIOperationStatus.Error.NotInitialised
        target.opiOperationState.getOrAwaitValue() shouldBe State.OperationError(
            data = OPIOperationStatus.Error.NotInitialised,
            message = "OPI Communication controller not initialised."
        )

        mutableOperationFlow.value = OPIOperationStatus.Error.Communication("comms err", throwable)
        target.opiOperationState.getOrAwaitValue() shouldBe State.OperationError(
            data = OPIOperationStatus.Error.Communication("comms err", throwable),
            message = "comms err"
        )

        mutableOperationFlow.value = OPIOperationStatus.Error.DataHandling("data err", throwable)
        target.opiOperationState.getOrAwaitValue() shouldBe State.OperationError(
            data = OPIOperationStatus.Error.DataHandling("data err", throwable),
            message = "data err"
        )

        mutableOperationFlow.value = OPIOperationStatus.Result.Error(
            date = instant,
            customerReceipt = "customerReceipt",
            merchantReceipt = "merchantReceipt",
            rawData = "rawData",
        )
        target.opiOperationState.getOrAwaitValue() shouldBe State.ResultError(
            data = TerminalOperationStatus.Error.OPI(
                date = instant,
                customerReceipt = "customerReceipt",
                merchantReceipt = "merchantReceipt",
                rawData = "rawData",
                data = null,
                resultCode = TransactionResultCode.Unknown(
                    resultCode = -1,
                    errorMessage = R.string.zvt_error_code_unknown
                )
            )
        )

        mutableOperationFlow.value = OPIOperationStatus.Result.Success(
            date = instant,
            customerReceipt = "customerReceipt",
            merchantReceipt = "merchantReceipt",
            rawData = "rawData",
        )
        target.opiOperationState.getOrAwaitValue() shouldBe State.ResultSuccess(
            data = TerminalOperationStatus.Success.OPI(
                date = instant,
                customerReceipt = "customerReceipt",
                merchantReceipt = "merchantReceipt",
                rawData = "rawData",
                data = null
            )
        )
    }

    test("onDestroy") {
        target.onDestroy()

        verify {
            opiChannelController.close()
        }
    }

    test("loginToTerminal") {
        target.loginToTerminal()

        coVerify {
            opiChannelController.login()
        }
    }

    test("startPayment") {
        target.startPayment(500.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

        coVerify {
            opiChannelController.initiateCardPayment(
                amount = 5.0.toBigDecimal().setScale(3),
                currency = ISOAlphaCurrency("EUR")
            )
        }
    }

    test("startPaymentReversal") {
        target.startPaymentReversal("stan")

        coVerify {
            opiChannelController.initiatePaymentReversal("stan")
        }
    }

    test("startPartialRefund") {
        target.startPartialRefund(600.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

        coVerify {
            opiChannelController.initiatePartialRefund(
                amount = 6.0.toBigDecimal().setScale(3),
                currency = ISOAlphaCurrency("EUR")
            )
        }
    }

    test("startReconciliation") {
        target.startReconciliation()

        coVerify {
            opiChannelController.initiateReconciliation()
        }
    }
})
