package de.tillhub.paymentengine.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
        it("json conversion results in correct data") {
            val transactionData = converter.convertFromJson(TRANSACTION_DATA).getOrNull()

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
    }
}
