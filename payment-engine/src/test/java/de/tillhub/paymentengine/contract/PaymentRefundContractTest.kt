package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.testing.TestTerminal
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

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class PaymentRefundContractTest : FunSpec({
    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: PaymentRefundContract

    val terminal: Terminal = TestTerminal("external")

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = PaymentRefundContract(analytics)
    }

    test("createIntent") {
        every { terminal.contract.refundIntent(any(), any()) } returns Intent("REFUND")

        val request = RefundRequest(
            terminal,
            "UUID",
            500.toBigDecimal(),
            ISOAlphaCurrency("EUR")
        )

        val result = target.createIntent(context, request)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "REFUND"

        verify {
            terminal.contract.refundIntent(context, request)
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
})