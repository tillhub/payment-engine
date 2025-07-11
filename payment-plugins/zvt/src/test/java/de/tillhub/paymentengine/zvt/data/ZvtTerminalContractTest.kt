package de.tillhub.paymentengine.zvt.data

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.BundleCompat
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class ZvtTerminalContractTest : FunSpec({
    lateinit var context: Context

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
    }

    test("connectIntent") {
        val result = ZvtTerminalContract.connectIntent(context, ZVT)

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.TerminalLoginActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            ZvtTerminal::class.java
        ) shouldBe ZVT
    }

    test("paymentIntent") {
        val result = ZvtTerminalContract.paymentIntent(
            context,
            PaymentRequest(
                ZVT,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.CardPaymentActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            ZvtTerminal::class.java
        ) shouldBe ZVT
        BundleCompat.getSerializable(
            result.extras!!,
            ExtraKeys.EXTRA_AMOUNT,
            BigDecimal::class.java
        ) shouldBe 600.toBigDecimal()
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CURRENCY,
            ISOAlphaCurrency::class.java
        ) shouldBe ISOAlphaCurrency("EUR")
    }

    test("refundIntent") {
        val result = ZvtTerminalContract.refundIntent(
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
            ZvtTerminal::class.java
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
    }

    test("reversalIntent") {
        val result = ZvtTerminalContract.reversalIntent(
            context,
            ReversalRequest(
                ZVT,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR"),
                "receiptNo"
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.CardPaymentReversalActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            ZvtTerminal::class.java
        ) shouldBe ZVT
        result.extras?.getString(ExtraKeys.EXTRA_RECEIPT_NO) shouldBe "receiptNo"
    }

    test("reconciliationIntent") {
        val result = ZvtTerminalContract.reconciliationIntent(context, ZVT)

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.zvt.ui.TerminalReconciliationActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            ZvtTerminal::class.java
        ) shouldBe ZVT
    }

    test("recoveryIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            ZvtTerminalContract.recoveryIntent(context, ZVT)
        }

        result.message shouldBe "Payment recovery is not supported by this terminal"
    }

    test("disconnectIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            ZvtTerminalContract.disconnectIntent(context, ZVT)
        }

        result.message shouldBe "Disconnect is not supported by this terminal"
    }

    test("ticketReprintIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            ZvtTerminalContract.ticketReprintIntent(context, ZVT)
        }

        result.message shouldBe "Ticket reprint is not supported by this terminal"
    }
}) {
    companion object {
        val ZVT = ZvtTerminal.create(
            id = "zvt",
            ipAddress = "127.0.0.1",
            port = 20007,
        )
    }
}
