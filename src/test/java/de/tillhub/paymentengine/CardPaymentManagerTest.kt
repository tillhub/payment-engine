package de.tillhub.paymentengine

import android.content.Context
import androidx.activity.ComponentActivity
import de.lavego.sdk.PaymentProtocol
import de.tillhub.paymentengine.data.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

class CardPaymentManagerTest : FunSpec({

    lateinit var lavegoTransactionDataConverter: LavegoTransactionDataConverter
    lateinit var terminalTime: TerminalTime
    lateinit var cardPaymentConfigRepository: CardPaymentConfigRepository
    lateinit var cardSaleConfigRepository: CardSaleConfigRepository
    lateinit var appContext: Context

    lateinit var activity: ComponentActivity

    lateinit var target: CardPaymentManagerImpl

    beforeTest {
        lavegoTransactionDataConverter = mockk {
            coEvery { convertFromJson(any()) } returns Payment.Success(LAVEGO_TRANSACTION_DATA)
        }

        terminalTime = mockk {
            every { now() } returns LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC)
        }

        appContext = mockk()
        activity = mockk()

        cardPaymentConfigRepository = mockk()
        cardSaleConfigRepository = mockk()

        target = CardPaymentManagerImpl(
            appContext,
            cardPaymentConfigRepository,
            cardSaleConfigRepository,
            terminalTime,
            lavegoTransactionDataConverter)
    }

    test("activeTerminalConnection + connect + disconnect") {
        target.activeTerminalConnection shouldBe null

        val connection = target.connect(activity)

        connection.shouldNotBeNull()
        target.activeTerminalConnection shouldBe connection

        target.disconnect(connection)

        target.activeTerminalConnection shouldBe null
    }

    test("payment transaction flow") {
        val connection: LavegoConnection = mockk<LavegoConnection> {
            every { startPaymentTransaction() } just Runs
        }.apply {
            target.activeTerminalConnection = this
        }

        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        target.startPaymentTransaction(BigDecimal(1000))
        target.transactionState shouldBe LavegoTerminalOperation.Pending.Payment(BigDecimal(1000))
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Pending.Payment(
            BigDecimal(1000)
        )

        target.onStatus("status_json")
        target.onReceipt("merchantReceipt")
        target.onReceipt("")
        target.onReceipt("customerReceipt")
        target.onCompletion("completion")
        target.transactionState shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.onError("error")
        target.transactionState shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.clearTransaction()
        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        coVerify(exactly = 1) { connection.startPaymentTransaction() }
        coVerify(exactly = 2) { lavegoTransactionDataConverter.convertFromJson("status_json") }
    }

    test("payment reversal transaction flow") {
        val connection: LavegoConnection = mockk<LavegoConnection> {
            every { startPaymentReversal() } just Runs
        }.apply {
            target.activeTerminalConnection = this
        }

        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        target.startReversalTransaction("receipt_number")
        target.transactionState shouldBe LavegoTerminalOperation.Pending.Reversal("receipt_number")
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Pending.Reversal("receipt_number")

        target.onStatus("status_json")
        target.onReceipt("merchantReceipt")
        target.onReceipt("")
        target.onReceipt("customerReceipt")
        target.onCompletion("completion")
        target.transactionState shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.onError("error")
        target.transactionState shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.clearTransaction()
        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        coVerify(exactly = 1) { connection.startPaymentReversal() }
        coVerify(exactly = 2) { lavegoTransactionDataConverter.convertFromJson("status_json") }
    }

    test("payment partial refund flow") {
        val connection: LavegoConnection = mockk<LavegoConnection> {
            every { startPartialRefund() } just Runs
        }.apply {
            target.activeTerminalConnection = this
        }

        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        target.startPartialRefundTransaction(BigDecimal(1000))
        target.transactionState shouldBe LavegoTerminalOperation.Pending.PartialRefund(BigDecimal(1000))
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Pending.PartialRefund(BigDecimal(1000))

        target.onStatus("status_json")
        target.onReceipt("merchantReceipt")
        target.onReceipt("")
        target.onReceipt("customerReceipt")
        target.onCompletion("completion")
        target.transactionState shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.onError("error")
        target.transactionState shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.clearTransaction()
        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        coVerify(exactly = 1) { connection.startPartialRefund() }
        coVerify(exactly = 2) { lavegoTransactionDataConverter.convertFromJson("status_json") }
    }

    test("terminal reconciliation flow") {
        val connection: LavegoConnection = mockk<LavegoConnection> {
            every { startReconciliation() } just Runs
        }.apply {
            target.activeTerminalConnection = this
        }

        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        target.startReconciliation()
        target.transactionState shouldBe LavegoTerminalOperation.Pending.Reconciliation
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Pending.Reconciliation

        target.onStatus("status_json")
        target.onReceipt("merchantReceipt")
        target.onReceipt("")
        target.onReceipt("customerReceipt")
        target.onCompletion("completion")
        target.transactionState shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Success(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.onError("error")
        target.transactionState shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Failed(
            date = LocalDate.of(2021, 1, 3).atTime(20, 12).toInstant(ZoneOffset.UTC),
            merchantReceipt = "merchantReceipt\n",
            customerReceipt = "customerReceipt\n",
            rawData = "status_json",
            data = LAVEGO_TRANSACTION_DATA
        )

        target.clearTransaction()
        target.transactionState shouldBe LavegoTerminalOperation.Waiting
        target.transactionStateFlow.value shouldBe LavegoTerminalOperation.Waiting

        coVerify(exactly = 1) { connection.startReconciliation() }
        coVerify(exactly = 2) { lavegoTransactionDataConverter.convertFromJson("status_json") }
    }

    test("getTransportConfiguration") {
        every { cardPaymentConfigRepository.config } returns CardPaymentConfig(
            ipAddress  = "128.0.0.1",
            port = 8000
        )

        val result = target.getTransportConfiguration()

        result.paymentProtocol shouldBe PaymentProtocol.Zvt
        result.host shouldBe "128.0.0.1"
        result.port shouldBe 8000
    }

    test("getSaleConfiguration") {
        every { cardSaleConfigRepository.config } returns CardSaleConfig()
        val result = target.getSaleConfiguration()

        result.applicationName shouldBe "Tillhub GO"
        result.operatorId shouldBe "ah"
        result.saleId shouldBe "registerProvider"
        result.pin shouldBe "333333"
        result.poiId shouldBe "66000001"
        result.poiSerialnumber shouldBe ""
        result.trainingMode shouldBe true
    }
}) {
    companion object {
        val LAVEGO_TRANSACTION_DATA = LavegoTransactionData(
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
            resultCode = 1,
            resultText = "resultText",
            singleAmounts = "singleAmounts",
            tags = null,
            tid = "tid",
            time = "20.5.2020 20:00:00",
            trace = "trace",
            traceOrig = "traceOrig",
            track1 = "track1",
            track2 = "track2",
            track3 = "track3",
            vu = "vu",
        )
    }
}
