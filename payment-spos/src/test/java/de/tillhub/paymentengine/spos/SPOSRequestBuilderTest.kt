package de.tillhub.paymentengine.spos

import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.spos.data.SPOSExtraKeys
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSRequestBuilderTest : FunSpec({

    test("buildPaymentRequest should return a PaymentRequest object") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
        }

        val result = SPOSRequestBuilder.buildPaymentRequest(intent)

        result.shouldBeInstanceOf<PaymentRequest>()
        result shouldBe PaymentRequest(
            config = SPOSTerminal(),
            transactionId = "transaction_id",
            amount = BigDecimal.valueOf(10.0),
            tip = BigDecimal.valueOf(1.0),
            currency = ISOAlphaCurrency("EUR")
        )
    }

    test("buildPaymentRequest should throw an exception if extras is null") {
        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildPaymentRequest(Intent())
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Extras is null"
    }

    test("buildRefundRequest should return a RefundRequest object") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
        }

        val result = SPOSRequestBuilder.buildRefundRequest(intent)

        result.shouldBeInstanceOf<RefundRequest>()
        result shouldBe RefundRequest(
            config = SPOSTerminal(),
            transactionId = "transaction_id",
            amount = BigDecimal.valueOf(10.0),
            currency = ISOAlphaCurrency("EUR")
        )
    }

    test("buildRefundRequest should throw an exception if extras is null") {
        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildRefundRequest(Intent())
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Extras is null"
    }

    test("buildReversalRequest should return a ReversalRequest object") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = SPOSRequestBuilder.buildReversalRequest(intent)

        result.shouldBeInstanceOf<ReversalRequest>()
        result shouldBe ReversalRequest(
            config = SPOSTerminal(),
            transactionId = "transaction_id",
            amount = BigDecimal.valueOf(10.0),
            tip = BigDecimal.valueOf(1.0),
            currency = ISOAlphaCurrency("EUR"),
            receiptNo = "receipt_no"
        )
    }

    test("buildReversalRequest should throw an exception if extras is null") {
        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(Intent())
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Extras is null"
    }

    test("buildReversalRequest should throw an exception if argument config is missing") {
        val intent = Intent().apply {
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument config is missing"
    }

    test("buildReversalRequest should throw an exception if argument tx id is missing") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument TxId is missing"
    }

    test("buildReversalRequest should throw an exception if argument amount is missing") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument amount is missing"
    }

    test("buildReversalRequest should throw an exception if argument tip is missing") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument tip is missing"
    }

    test("buildReversalRequest should throw an exception if argument currency is missing") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, "receipt_no")
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument currency is missing"
    }

    test("buildReversalRequest should throw an exception if argument receipt no is missing") {
        val intent = Intent().apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, SPOSTerminal())
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, "transaction_id")
            putExtra(ExtraKeys.EXTRA_AMOUNT, BigDecimal.valueOf(10.0))
            putExtra(SPOSExtraKeys.EXTRA_TIP, BigDecimal.valueOf(1.0))
            putExtra(ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency("EUR"))
        }

        val result = shouldThrow<IllegalArgumentException> {
            SPOSRequestBuilder.buildReversalRequest(intent)
        }

        result.shouldBeInstanceOf<IllegalArgumentException>()
        result.message shouldBe "TerminalActivity: Argument ReceiptNo is missing"
    }
})
