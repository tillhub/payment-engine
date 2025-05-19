package de.tillhub.paymentengine.contract

import android.content.Context
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
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
class TerminalDisconnectContractTest : FunSpec({

    lateinit var context: Context
    lateinit var analytics: PaymentAnalytics

    lateinit var target: TerminalDisconnectContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        target = TerminalDisconnectContract(analytics)
    }

    test("createIntent OPI + ZVT") {
        try {
            target.createIntent(
                context,
                OPI
            )
        } catch (e: Exception) {
            e.shouldBeInstanceOf<UnsupportedOperationException>()
            e.message shouldBe "Disconnect is not supported by this terminal"
        }
        verify(inverse = true) {
            analytics.logOperation(any())
        }
    }
}) {
    companion object {
        val OPI = Terminal.OPI(
            id = "opi",
            ipAddress = "127.0.0.1",
            port = 20002,
            port2 = 20007
        )
    }
}