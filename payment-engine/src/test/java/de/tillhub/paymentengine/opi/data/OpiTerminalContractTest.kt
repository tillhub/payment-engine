package de.tillhub.paymentengine.opi.data

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
class OpiTerminalContractTest : FunSpec({
    lateinit var context: Context

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
    }

    test("connectIntent") {
        val result = OpiTerminalContract.connectIntent(context, OPI)

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPILoginActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            OpiTerminal::class.java
        ) shouldBe OPI
    }

    test("paymentIntent") {
        val result = OpiTerminalContract.paymentIntent(
            context,
            PaymentRequest(
                OPI,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPIPaymentActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            OpiTerminal::class.java
        ) shouldBe OPI
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
        val result = OpiTerminalContract.refundIntent(
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
            OpiTerminal::class.java
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
    }

    test("reversalIntent") {
        val result = OpiTerminalContract.reversalIntent(
            context,
            ReversalRequest(
                OPI,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR"),
                "receiptNo"
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPIPaymentReversalActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            OpiTerminal::class.java
        ) shouldBe OPI
        result.extras?.getString(ExtraKeys.EXTRA_RECEIPT_NO) shouldBe "receiptNo"
    }

    test("reconciliationIntent") {
        val result = OpiTerminalContract.reconciliationIntent(context, OPI)

        result.shouldBeInstanceOf<Intent>()
        result.component?.className shouldBe "de.tillhub.paymentengine.opi.ui.OPIReconciliationActivity"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            OpiTerminal::class.java
        ) shouldBe OPI
    }

    test("recoveryIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            OpiTerminalContract.recoveryIntent(context, OPI)
        }

        result.message shouldBe "Payment recovery is not supported by this terminal"
    }

    test("disconnectIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            OpiTerminalContract.disconnectIntent(context, OPI)
        }

        result.message shouldBe "Disconnect is not supported by this terminal"
    }

    test("ticketReprintIntent") {
        val result = shouldThrow<UnsupportedOperationException> {
            OpiTerminalContract.ticketReprintIntent(context, OPI)
        }

        result.message shouldBe "Ticket reprint is not supported by this terminal"
    }
}) {
    companion object {
        val OPI = OpiTerminal.create(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
    }
}
