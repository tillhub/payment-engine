package de.tillhub.paymentengine.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TerminalOperationSuccess
import de.tillhub.paymentengine.testing.TestExternalTerminal
import io.kotest.assertions.throwables.shouldThrow
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
class PaymentRecoveryContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: PaymentRecoveryContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = PaymentRecoveryContract(analytics)
    }

    test("createIntent External") {
        val result = target.createIntent(context, TestExternalTerminal("external"))

        verify {
            analytics.logOperation(any())
        }

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "RECOVERY"
    }

    test("createIntent OPI") {
        val result = shouldThrow<UnsupportedOperationException> {
            target.createIntent(context, Terminal.OPI())
        }

        verify(inverse = true) {
            analytics.logOperation(any())
        }

        result.message shouldBe "Payment recovery is not supported by this terminal"
    }

    test("createIntent ZVT") {
        val result = shouldThrow<UnsupportedOperationException> {
            target.createIntent(context, Terminal.ZVT())
        }

        verify(inverse = true) {
            analytics.logOperation(any())
        }

        result.message shouldBe "Payment recovery is not supported by this terminal"
    }

    test("parseResult: result OK") {
        val intent = Intent().apply {
            putExtra(
                ExtraKeys.EXTRAS_RESULT,
                TerminalOperationStatus.Recovery.Success(
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

        result.shouldBeInstanceOf<TerminalOperationStatus.Recovery.Success>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }

    test("parseResult: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Recovery.Canceled>()

        verify {
            analytics.logCommunication(any(), any())
        }
    }
})