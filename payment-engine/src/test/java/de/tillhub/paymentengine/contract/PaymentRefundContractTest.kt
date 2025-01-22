package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.BundleCompat
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class PaymentRefundContractTest : FunSpec({
    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: PaymentRefundContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = PaymentRefundContract(analytics)
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            RefundRequest(
                SPOS,
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.TRANSACTION"
        result.extras?.getString(SPOSKey.Extra.TRANSACTION_TYPE) shouldBe "PaymentRefund"
        result.extras?.getString(SPOSKey.Extra.CURRENCY_ISO) shouldBe "EUR"
        result.extras?.getString(SPOSKey.Extra.AMOUNT) shouldBe "500"
        result.extras?.getString(SPOSKey.Extra.TRANSACTION_ID) shouldBe "UUID"
        result.extras?.getString(SPOSKey.Extra.TAX_AMOUNT) shouldBe "000"

        verify {
            analytics.logOperation(
                "Operation: PARTIAL_REFUND(" +
                        "amount: 500, " +
                        "currency: ISOAlphaCurrency(value=EUR))" +
                        "\nTerminal.SPOS(" +
                        "id=s-pos, " +
                        "appId=TESTCLIENT, " +
                        "saleConfig=CardSaleConfig(" +
                        "applicationName=Tillhub GO, " +
                        "operatorId=ah, " +
                        "saleId=registerProvider, " +
                        "pin=333333, " +
                        "poiId=66000001, " +
                        "poiSerialNumber=" +
                        "), " +
                        "currencyCode=EUR" +
                        ")"
            )
        }
    }

    test("createIntent OPI") {
        val result = target.createIntent(
            context,
            RefundRequest(
                OPI,
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPIPartialRefundActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            Terminal.OPI::class.java
        ) shouldBe OPI
        BundleCompat.getSerializable(
            result.extras!!,
            ExtraKeys.EXTRA_AMOUNT,
            BigDecimal::class.java
        ) shouldBe 500.toBigDecimal()
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CURRENCY,
            ISOAlphaCurrency::class.java
        ) shouldBe ISOAlphaCurrency("EUR")

        verify {
            analytics.logOperation(
                "Operation: PARTIAL_REFUND(" +
                        "amount: 500, " +
                        "currency: ISOAlphaCurrency(value=EUR))" +
                        "\nTerminal.OPI(" +
                        "id=opi, " +
                        "ipAddress=127.0.0.1, " +
                        "port=20002, " +
                        "saleConfig=CardSaleConfig(" +
                        "applicationName=Tillhub GO, " +
                        "operatorId=ah, " +
                        "saleId=registerProvider, " +
                        "pin=333333, " +
                        "poiId=66000001, " +
                        "poiSerialNumber=" +
                        "), " +
                        "port2=20007, " +
                        "currencyCode=EUR" +
                        ")"
            )
        }
    }

    test("createIntent ZVT") {
        val result = target.createIntent(
            context,
            RefundRequest(
                ZVT,
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.CardPaymentPartialRefundActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            Terminal.ZVT::class.java
        ) shouldBe ZVT
        BundleCompat.getSerializable(
            result.extras!!,
            ExtraKeys.EXTRA_AMOUNT,
            BigDecimal::class.java
        ) shouldBe 500.toBigDecimal()
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CURRENCY,
            ISOAlphaCurrency::class.java
        ) shouldBe ISOAlphaCurrency("EUR")

        verify {
            analytics.logOperation(
                "Operation: PARTIAL_REFUND(" +
                        "amount: 500, " +
                        "currency: ISOAlphaCurrency(value=EUR))" +
                        "\nTerminal.ZVT(" +
                        "id=zvt, " +
                        "ipAddress=127.0.0.1, " +
                        "port=40007, " +
                        "saleConfig=CardSaleConfig(" +
                        "applicationName=Tillhub GO, " +
                        "operatorId=ah, " +
                        "saleId=registerProvider, " +
                        "pin=333333, " +
                        "poiId=66000001, " +
                        "poiSerialNumber=" +
                        "), " +
                        "terminalPrinterAvailable=true, " +
                        "isoCurrencyNumber=0978" +
                        ")"
            )
        }
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.RESULT_STATE, "Success")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_RESULT, "ACCEPTED")
            putExtra(SPOSKey.ResultExtra.TERMINAL_ID, "terminal_id")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_DATA, "transaction_data")
            putExtra(SPOSKey.ResultExtra.CARD_CIRCUIT, "card_circuit")
            putExtra(SPOSKey.ResultExtra.CARD_PAN, "card_pan")
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Success.SPOS>()
        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT OK\nExtras {\n" +
                        "${SPOSKey.ResultExtra.CARD_PAN} = card_pan\n" +
                        "${SPOSKey.ResultExtra.CARD_CIRCUIT} = card_circuit\n" +
                        "${SPOSKey.ResultExtra.TRANSACTION_DATA} = transaction_data\n" +
                        "${SPOSKey.ResultExtra.TERMINAL_ID} = terminal_id\n" +
                        "${SPOSKey.ResultExtra.TRANSACTION_RESULT} = ACCEPTED\n" +
                        "${SPOSKey.ResultExtra.RESULT_STATE} = Success\n" +
                        "}"
            )
        }
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.ERROR, "CARD_PAYMENT_NOT_ONBOARDED")
        }

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Error.SPOS>()

        verify {
            analytics.logCommunication(
                protocol = "SPOS",
                message = "RESPONSE: RESULT CANCELED\nExtras {\n" +
                        "ERROR = CARD_PAYMENT_NOT_ONBOARDED\n" +
                        "}"
            )
        }
    }

    test("parseResult OPI + ZVT: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Success.OPI(
                    date = mockk(),
                    customerReceipt = "customerReceipt",
                    merchantReceipt = "merchantReceipt",
                    rawData = "rawData",
                    data = null
                )
            )
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Success.OPI>()
    }

    test("parseResult OPI + ZVT: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Canceled>()
    }
}) {
    companion object {
        val ZVT = Terminal.ZVT(
            id = "zvt",
            ipAddress = "127.0.0.1",
            port = 40007,
        )
        val OPI = Terminal.OPI(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
        val SPOS = Terminal.SPOS(
            id = "s-pos",
        )
    }
}