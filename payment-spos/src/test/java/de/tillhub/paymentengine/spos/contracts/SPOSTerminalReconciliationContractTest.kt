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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.spyk
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RobolectricTest
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class SPOSTerminalReconciliationContractTest : FunSpec({
    lateinit var context: Context

    lateinit var target: SPOSTerminalReconciliationContract

    beforeTest {
        context = spyk(RuntimeEnvironment.getApplication())

        target = SPOSTerminalReconciliationContract()
    }

    test("createIntent SPOS") {
        val result = target.createIntent(
            context,
            SPOS
        )

        result.shouldBeInstanceOf<Intent>()
        result.action shouldBe "de.spayment.akzeptanz.RECONCILIATION"
        result.extras.shouldBeNull()
    }

    test("parseResult SPOS: result OK") {
        val intent = Intent().apply {
            putExtra(SPOSKey.ResultExtra.RESULT_STATE, "Success")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_RESULT, "ACCEPTED")
            putExtra(SPOSKey.ResultExtra.TERMINAL_ID, "terminal_id")
            putExtra(SPOSKey.ResultExtra.TRANSACTION_DATA, "transaction_data")
            putExtra(SPOSKey.ResultExtra.CARD_CIRCUIT, "card_circuit")
            putExtra(SPOSKey.ResultExtra.CARD_PAN, "card_pan")
        }

        val result = target.parseResult(Activity.RESULT_OK, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Reconciliation.Success>()
    }

    test("parseResult SPOS: result CANCELED") {
        val intent = Intent()

        val result = target.parseResult(Activity.RESULT_CANCELED, intent)

        result.shouldBeInstanceOf<TerminalOperationStatus.Reconciliation.Canceled>()
    }
}) {
    companion object {
        val SPOS = SposTerminal.create(
            id = "s-pos",
        )
    }
}