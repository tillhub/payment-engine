package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import java.time.Instant

@ExperimentalCoroutinesApi
class OPIServiceTest : FunSpec({
    lateinit var terminal: Terminal.OPI
    lateinit var instant: Instant
    lateinit var throwable: Throwable

    lateinit var opiChannelController: OPIChannelController

    lateinit var target: OPIService

    val mutableOperationFlow = MutableStateFlow<OPIOperationStatus>(OPIOperationStatus.Idle)

    beforeTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())

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

        target = OPIService(opiChannelController)
    }

    test("init") {
        target.init(terminal)

        verify(Ordering.ORDERED) {
            opiChannelController.init(terminal)
            opiChannelController.operationState
        }
    }

    test("opiOperationState") {
        val callback: Callback = mockk(relaxed = true)
        target.setBringToFront(callback::method)

        target.init(terminal)

        target.opiOperationState.value shouldBe OPIService.State.Idle

        mutableOperationFlow.value = OPIOperationStatus.Pending.Login
        target.opiOperationState.value shouldBe OPIService.State.Pending.Login

        mutableOperationFlow.value = OPIOperationStatus.Pending.Operation(
            date = instant,
            messageLines = emptyList()
        )
        target.opiOperationState.value shouldBe OPIService.State.Pending.NoMessage

        mutableOperationFlow.value = OPIOperationStatus.Pending.Operation(
            date = instant,
            messageLines = listOf(
                "line 1",
                "line 2",
                "line 3"
            )
        )
        target.opiOperationState.value shouldBe OPIService.State.Pending.WithMessage(
            "line 1\nline 2\nline 3\n"
        )

        mutableOperationFlow.value = OPIOperationStatus.LoggedIn
        target.opiOperationState.value shouldBe OPIService.State.LoggedIn

        mutableOperationFlow.value = OPIOperationStatus.Error.NotInitialised
        target.opiOperationState.value shouldBe OPIService.State.OperationError(
            data = OPIOperationStatus.Error.NotInitialised,
            message = "OPI Communication controller not initialised."
        )

        mutableOperationFlow.value = OPIOperationStatus.Error.Communication("comms err", throwable)
        target.opiOperationState.value shouldBe OPIService.State.OperationError(
            data = OPIOperationStatus.Error.Communication("comms err", throwable),
            message = "comms err"
        )

        mutableOperationFlow.value = OPIOperationStatus.Error.DataHandling("data err", throwable)
        target.opiOperationState.value shouldBe OPIService.State.OperationError(
            data = OPIOperationStatus.Error.DataHandling("data err", throwable),
            message = "data err"
        )

        mutableOperationFlow.value = OPIOperationStatus.Result.Error(
            date = instant,
            customerReceipt = "customerReceipt",
            merchantReceipt = "merchantReceipt",
            rawData = "rawData",
        )
        target.opiOperationState.value shouldBe OPIService.State.ResultError(
            data = TerminalOperationError(
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
        target.opiOperationState.value shouldBe OPIService.State.ResultSuccess(
            data = TerminalOperationSuccess(
                date = instant,
                customerReceipt = "customerReceipt",
                merchantReceipt = "merchantReceipt",
                rawData = "rawData",
                data = null
            )
        )

        verify(exactly = 2) {
            callback.method()
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
}) {
    interface Callback {
        fun method()
    }
}
