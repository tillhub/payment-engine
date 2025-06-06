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
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.opi.data.OPITerminal
import de.tillhub.paymentengine.testing.TestExternalTerminal
import de.tillhub.paymentengine.zvt.data.ZVTTerminal
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

    test("createIntent External") {
        val result = target.createIntent(
            context,
            RefundRequest(
                TestExternalTerminal("external"),
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "REFUND"

        verify {
            analytics.logOperation(
                "Operation: PARTIAL_REFUND(" +
                        "amount: 500, " +
                        "currency: ISOAlphaCurrency(value=EUR))" +
                        "\nTerminal.External(" +
                        "id=external, " +
                        "saleConfig=CardSaleConfig(" +
                        "applicationName=Tillhub GO, " +
                        "operatorId=ah, " +
                        "saleId=registerProvider, " +
                        "pin=333333, " +
                        "poiId=66000001, " +
                        "poiSerialNumber=" +
                        ")" +
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
            OPITerminal::class.java
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
                        "\nOPITerminal(" +
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
            ZVTTerminal::class.java
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
                        "\nZVTTerminal(" +
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

    test("parseResult: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Refund.Success(
                    TerminalOperationSuccess(
                        date = mockk(),
                        customerReceipt = "customerReceipt",
                        merchantReceipt = "merchantReceipt",
                        rawData = "rawData",
                        data = null
                    )
                )
            )
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Refund.Success>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }

    test("parseResult: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Refund.Canceled>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }
}) {
    companion object {
        val ZVT = ZVTTerminal(
            id = "zvt",
            ipAddress = "127.0.0.1",
            port = 40007,
        )
        val OPI = OPITerminal(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
    }
}