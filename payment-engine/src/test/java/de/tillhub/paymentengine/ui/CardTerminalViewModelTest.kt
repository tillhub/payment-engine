package de.tillhub.paymentengine.ui

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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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
    }
}
