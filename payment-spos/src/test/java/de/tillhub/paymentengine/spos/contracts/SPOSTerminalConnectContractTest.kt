package de.tillhub.paymentengine.spos.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.spos.data.SPOSKey
import de.tillhub.paymentengine.spos.data.SposTerminal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSTerminalConnectContractTest : FunSpec({

    lateinit var context: Context
    lateinit var target: SPOSTerminalConnectContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())

        target = SPOSTerminalConnectContract()
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            SPOS
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.S_SWITCH_CONNECT"
        result.extras?.getString(SPOSKey.Extra.APP_ID) shouldBe "TESTCLIENT"
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Connected>()
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.ERROR, "CARD_PAYMENT_NOT_ONBOARDED")
        }

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Login.Error>()
    }
}) {
    companion object {
        val SPOS = SposTerminal.create(
            id = "s-pos",
        )
    }
}
