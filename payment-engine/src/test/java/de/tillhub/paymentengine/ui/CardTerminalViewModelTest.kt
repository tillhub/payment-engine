package de.tillhub.paymentengine.ui

import android.util.Log
import de.tillhub.paymentengine.R
import de.tillhub.paymentengine.zvt.data.LavegoTransactionData
import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.helper.CoreTestCoroutineDispatcher
import de.tillhub.paymentengine.helper.InstantTaskExecutor
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.zvt.ui.CardTerminalViewModel
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.math.BigInteger
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class CardTerminalViewModelTest : FunSpec({

    val date: Instant = Instant.now()
    lateinit var terminalConfig: TerminalConfig
    lateinit var viewModel: CardTerminalViewModel

    lateinit var callback: Callback

    val instantTaskExecutor = InstantTaskExecutor()

    beforeSpec {
        Dispatchers.setMain(CoreTestCoroutineDispatcher())
        instantTaskExecutor.setupLiveData()
    }

    beforeTest {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0

        terminalConfig = mockk {
            every { timeNow() } returns date
        }

        callback = mockk(relaxed = true)

        viewModel = CardTerminalViewModel(terminalConfig)
    }

    afterSpec {
        Dispatchers.resetMain()
        instantTaskExecutor.resetLiveData()
    }

    test("terminalOperationState") {
        viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Idle
    }

    test("init") {
        viewModel.init()
        viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Setup
    }

    test("onCompletion").config(coroutineDebugProbes = true) {
        viewModel.init()
        viewModel.onCompletion(callback::moveToFront)

        viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Operation

        viewModel.onStatus(TRANSACTION_DATA)
        viewModel.onReceipt("receipt")
        viewModel.onCompletion(callback::moveToFront)

        verify(exactly = 1) {
            callback.moveToFront()
        }

        eventually(1.seconds) {
            viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Success(
                date = date,
                customerReceipt = "",
                merchantReceipt = "receipt\n",
                rawData = TRANSACTION_DATA,
                data = LavegoTransactionData(
                    additionalText = "additionalText",
                    aid = "aid",
                    amount = 1000.toBigInteger(),
                    cardName = "cardName",
                    cardType = 1,
                    cardSeqNumber = 1,
                    chipData = "chipData",
                    data = "data",
                    date = "20.5.2020",
                    expiry = "expiry",
                    receiptNo = 1,
                    resultCode = 2,
                    resultText = "resultText",
                    singleAmounts = "singleAmounts",
                    tags = mutableMapOf<String, String>().apply {
                        put("1f16", "3118")
                        put("1f17", "416e20756e737570706f7274656420636173686965722072657175657374207761732073656e74")
                    },
                    tid = "tid",
                    time = "20.5.2020 20:00:00",
                    trace = "trace",
                    traceOrig = "traceOrig",
                    track1 = "track1",
                    track2 = "track2",
                    track3 = "track3",
                    vu = "vu",
                )
            )
        }
    }

    test("parseTransactionNumber: Failure") {
        val result = viewModel.parseTransactionNumber("notANumber")
        result.isFailure.shouldBeTrue()
        result.onFailure {
            it.shouldBeInstanceOf<NumberFormatException>()
        }
    }

    test("parseTransactionNumber: Success") {
        val result = viewModel.parseTransactionNumber("1234")
        result.isSuccess.shouldBeTrue()
    }

    test("setupFinished").config(coroutineDebugProbes = true) {
        viewModel.init()

        viewModel.setupFinished(LOGIN_DATA, callback::moveToFront)

        eventually(1.seconds) {
            verify(exactly = 1) {
                callback.moveToFront()
            }

            viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Success(
                date = date,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = LOGIN_DATA,
                data = LavegoTransactionData(
                    additionalText = "",
                    aid = "",
                    amount = BigInteger.ZERO,
                    cardName = "",
                    cardSeqNumber = 0,
                    cardType = 0,
                    chipData = "",
                    data = "",
                    date = "",
                    expiry = "",
                    receiptNo = 0,
                    resultCode = 0,
                    resultText = "",
                    singleAmounts = "",
                    tags = emptyMap(),
                    tid = "526017",
                    time = "",
                    trace = "",
                    traceOrig = "",
                    track1 = "",
                    track2 = "",
                    track3 = "",
                    vu = ""
                )
            )
        }
    }

    test("onError") {
        viewModel.init()
        viewModel.onStatus(TRANSACTION_DATA)
        viewModel.onReceipt("receipt")
        viewModel.onError(callback::moveToFront)

        verify(exactly = 1) {
            callback.moveToFront()
        }

        eventually(1.seconds) {
            viewModel.terminalOperationState.value shouldBe CardTerminalViewModel.State.Error(
                date = date,
                customerReceipt = "",
                merchantReceipt = "receipt\n",
                rawData = TRANSACTION_DATA,
                data = LavegoTransactionData(
                    additionalText = "additionalText",
                    aid = "aid",
                    amount = 1000.toBigInteger(),
                    cardName = "cardName",
                    cardType = 1,
                    cardSeqNumber = 1,
                    chipData = "chipData",
                    data = "data",
                    date = "20.5.2020",
                    expiry = "expiry",
                    receiptNo = 1,
                    resultCode = 2,
                    resultText = "resultText",
                    singleAmounts = "singleAmounts",
                    tags = mutableMapOf<String, String>().apply {
                        put("1f16", "3118")
                        put("1f17", "416e20756e737570706f7274656420636173686965722072657175657374207761732073656e74")
                    },
                    tid = "tid",
                    time = "20.5.2020 20:00:00",
                    trace = "trace",
                    traceOrig = "traceOrig",
                    track1 = "track1",
                    track2 = "track2",
                    track3 = "track3",
                    vu = "vu",
                ),
                resultCode = TransactionResultCode.Known(
                    errorMessage = R.string.lavego_result_code_2_call_merchant
                )
            )
        }
    }
}) {

    interface Callback {
        fun moveToFront()
    }

    companion object {
        internal const val TRANSACTION_DATA: String = """
{
    "additional_text":"additionalText",
    "aid":"aid",
    "amount":1000,
    "card_name":"cardName",
    "card_seq_no":1,
    "card_type":1,
    "chip_data":"chipData",
    "data":"data",
    "date":"20.5.2020",
    "expiry":"expiry",
    "receipt_no":1,
    "resultCode":2,
    "resultText":"resultText",
    "single_amounts":"singleAmounts",
    "tags": {
        "1f16": "3118",
        "1f17": "416e20756e737570706f7274656420636173686965722072657175657374207761732073656e74"
    },
    "tid":"tid",
    "time":"20.5.2020 20:00:00",
    "trace":"trace",
    "trace_orig":"traceOrig",
    "track1":"track1",
    "track2":"track2",
    "track3":"track3",
    "vu":"vu"
}
"""

        internal const val LOGIN_DATA: String = """
{
  "fillingMachineMode" : false,
  "needDiagnosis" : false,
  "needInitialisation" : false,
  "needOPTAction" : false,
  "status" : 0,
  "vendingMachineMode" : false,
  "initialCommand" : "CMD_0600",
  "response_apdu" : "060fff4501190029526017354909780682013727031401fe1201302681d40a0205010a0206000a0206010a0206020a0206030a0206050a02060a0a02060c0a0206120a0206180a02061a0a02061b0a0206200a0206210a0206220a0206230a0206240a0206250a0206260a0206300a0206310a0206500a0206520a0206700a0206850a0206860a0206870a0206880a0206930a0206b00a0206c00a0206c10a0206c20a0206c30a0206c40a0206c50a0206c60a0206d10a0206d30a0206e00a0206e10a0206e20a0206e30a0206e50a0206e60a0206e70a0208010a0208100a0208110a0208120a0208130a0208300a0208501f71550f12141b1d2526272d4041e3e81f011f021f031f061f0d1f151f1f1f251f2e1f2f1f321f331f351f361f461f481f491f4a1f4b1f4c1f5b1f601f611f621f631f6b1f6d1f6e1f701f721f761f771f781f80001f8004"
}
"""
    }
}
