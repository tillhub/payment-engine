package de.tillhub.paymentengine.spos.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SPOSTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSTerminalDisconnectContractTest : FunSpec({

    lateinit var context: Context

    lateinit var target: SPOSTerminalDisconnectContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())

        target = SPOSTerminalDisconnectContract()
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            SPOSTerminal()
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.S_SWITCH_DISCONNECT"
        result.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe "TESTCLIENT"
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Disconnected>()
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Canceled>()
    }
})