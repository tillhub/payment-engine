package de.tillhub.paymentengine.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.tillhub.paymentengine.zvt.data.LavegoLoginData
import de.tillhub.paymentengine.zvt.data.LavegoTransactionData
import de.tillhub.paymentengine.zvt.data.LavegoTransactionDataConverter
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LavegoTransactionDataConverterTest : DescribeSpec({

    lateinit var moshi: Moshi
    lateinit var converter: LavegoTransactionDataConverter

    beforeAny {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        converter = LavegoTransactionDataConverter(moshi)
    }

    describe("convertFromJson") {
        it("tx json conversion results in correct data") {
            val transactionData = converter.convertFromTxJson(TRANSACTION_DATA).getOrNull()

            transactionData shouldBe LavegoTransactionData(
                additionalText = "additionalText",
                aid = "aid",
                amount = 1000.toBigInteger(),
                cardName = "cardName",
                cardType = 1,
                cardSeqNumber = 1,
                chipData = "chipData",
                data = "data",
                date = "20.5.2020",
                expiry = "expiry",
                receiptNo = 1,
                resultCode = 1,
                resultText = "resultText",
                singleAmounts = "singleAmounts",
                tags = mutableMapOf<String, String>().apply {
                    put("1f16", "3118")
                    put("1f17", "416e20756e737570706f7274656420636173686965722072657175657374207761732073656e74")
                },
                tid = "tid",
                time = "20.5.2020 20:00:00",
                trace = "trace",
                traceOrig = "traceOrig",
                track1 = "track1",
                track2 = "track2",
                track3 = "track3",
                vu = "vu",
            )
        }

        it("login json conversion results in correct data") {
            val loginData = converter.convertFromLoginJson(LOGIN_DATA).getOrNull()

            loginData shouldBe LavegoLoginData(APDU)
        }
    }
}) {
    companion object {
        internal const val TRANSACTION_DATA: String = """
{
    "additional_text":"additionalText",
    "aid":"aid",
    "amount":1000,
    "card_name":"cardName",
    "card_seq_no":1,
    "card_type":1,
    "chip_data":"chipData",
    "data":"data",
    "date":"20.5.2020",
    "expiry":"expiry",
    "receipt_no":1,
    "resultCode":1,
    "resultText":"resultText",
    "single_amounts":"singleAmounts",
    "tags": {
        "1f16": "3118",
        "1f17": "416e20756e737570706f7274656420636173686965722072657175657374207761732073656e74"
    },
    "tid":"tid",
    "time":"20.5.2020 20:00:00",
    "trace":"trace",
    "trace_orig":"traceOrig",
    "track1":"track1",
    "track2":"track2",
    "track3":"track3",
    "vu":"vu"
}
"""

        internal const val APDU = "060fff4501190029526017354909780682013727031401fe1201302681d40a020" +
                "5010a0206000a0206010a0206020a0206030a0206050a02060a0a02060c0a0206120a0206180a02061a" +
                "0a02061b0a0206200a0206210a0206220a0206230a0206240a0206250a0206260a0206300a0206310a0" +
                "206500a0206520a0206700a0206850a0206860a0206870a0206880a0206930a0206b00a0206c00a0206" +
                "c10a0206c20a0206c30a0206c40a0206c50a0206c60a0206d10a0206d30a0206e00a0206e10a0206e20" +
                "a0206e30a0206e50a0206e60a0206e70a0208010a0208100a0208110a0208120a0208130a0208300a02" +
                "08501f71550f12141b1d2526272d4041e3e81f011f021f031f061f0d1f151f1f1f251f2e1f2f1f321f3" +
                "31f351f361f461f481f491f4a1f4b1f4c1f5b1f601f611f621f631f6b1f6d1f6e1f701f721f761f771f" +
                "781f80001f8004"

        internal const val LOGIN_DATA: String = """
{
  "fillingMachineMode" : false,
  "needDiagnosis" : false,
  "needInitialisation" : false,
  "needOPTAction" : false,
  "status" : 0,
  "vendingMachineMode" : false,
  "initialCommand" : "CMD_0600",
  "response_apdu" : "$APDU"
}
"""
    }
}
