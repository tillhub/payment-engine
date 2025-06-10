package de.tillhub.paymentengine.spos.data

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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SposTerminalContractTest : FunSpec({
    lateinit var context: Context

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
    }

    test("connectIntent") {
        val result = SposTerminalContract.connectIntent(context, SPOS)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_CONNECT"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS

        result.`package` shouldBe context.packageName
    }

    test("paymentIntent") {
        val result = SposTerminalContract.paymentIntent(
            context,
            PaymentRequest(
                SPOS,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_PAYMENT"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS
        BundleCompat.getSerializable(
            result.extras!!,
            ExtraKeys.EXTRA_AMOUNT,
            BigDecimal::class.java
        ) shouldBe 500.toBigDecimal()
        BundleCompat.getSerializable(
            result.extras!!,
            SPOSExtraKeys.EXTRA_TIP,
            BigDecimal::class.java
        ) shouldBe 100.toBigDecimal()
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CURRENCY,
            ISOAlphaCurrency::class.java
        ) shouldBe ISOAlphaCurrency("EUR")
        result.getStringExtra(SPOSExtraKeys.EXTRA_TX_ID) shouldBe "UUID"

        result.`package` shouldBe context.packageName
    }

    test("refundIntent") {
        val result = SposTerminalContract.refundIntent(
            context,
            RefundRequest(
                SPOS,
                "UUID",
                500.toBigDecimal(),
                ISOAlphaCurrency("EUR")
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_REFUND"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS
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
        result.getStringExtra(SPOSExtraKeys.EXTRA_TX_ID) shouldBe "UUID"

        result.`package` shouldBe context.packageName
    }

    test("reversalIntent") {
        val result = SposTerminalContract.reversalIntent(
            context,
            ReversalRequest(
                SPOS,
                "UUID",
                500.toBigDecimal(),
                100.toBigDecimal(),
                ISOAlphaCurrency("EUR"),
                "receiptNo"
            )
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_REVERSAL"
        result.getStringExtra(ExtraKeys.EXTRA_RECEIPT_NO) shouldBe "receiptNo"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS
        BundleCompat.getSerializable(
            result.extras!!,
            ExtraKeys.EXTRA_AMOUNT,
            BigDecimal::class.java
        ) shouldBe 500.toBigDecimal()
        BundleCompat.getSerializable(
            result.extras!!,
            SPOSExtraKeys.EXTRA_TIP,
            BigDecimal::class.java
        ) shouldBe 100.toBigDecimal()
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CURRENCY,
            ISOAlphaCurrency::class.java
        ) shouldBe ISOAlphaCurrency("EUR")
        result.getStringExtra(SPOSExtraKeys.EXTRA_TX_ID) shouldBe "UUID"

        result.`package` shouldBe context.packageName
    }

    test("reconciliationIntent") {
        val result = SposTerminalContract.reconciliationIntent(context, SPOS)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_RECONCILIATION"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS

        result.`package` shouldBe context.packageName
    }

    test("recoveryIntent") {
        val result = SposTerminalContract.recoveryIntent(context, SPOS)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_RECOVERY"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS

        result.`package` shouldBe context.packageName
    }

    test("disconnectIntent") {
        val result = SposTerminalContract.disconnectIntent(context, SPOS)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_DISCONNECT"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS

        result.`package` shouldBe context.packageName
    }

    test("ticketReprintIntent") {
        val result = SposTerminalContract.ticketReprintIntent(context, SPOS)

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.tillhub.paymentengine.spos.ACTION_SPOS"
        result.getStringExtra(SPOSExtraKeys.EXTRA_ACTION) shouldBe "de.tillhub.paymentengine.spos.ACTION_REPRINT"
        BundleCompat.getParcelable(
            result.extras!!,
            ExtraKeys.EXTRA_CONFIG,
            SposTerminal::class.java
        ) shouldBe SPOS

        result.`package` shouldBe context.packageName
    }
}) {
    companion object {
        val SPOS = SposTerminal.create("spos")
    }
}
