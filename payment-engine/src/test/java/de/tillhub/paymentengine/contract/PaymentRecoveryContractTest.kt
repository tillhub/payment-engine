package de.tillhub.paymentengine.contract

import android.content.Context
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
})