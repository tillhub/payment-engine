package de.tillhub.paymentengine.zvt.data

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.tillhub.paymentengine.data.Payment
import de.tillhub.paymentengine.data.errorIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigInteger

internal class LavegoTransactionDataConverter(
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
) {
    private fun createTxJsonAdapter(): JsonAdapter<TransactionDataDto> =
        moshi.adapter(TransactionDataDto::class.java)

    suspend fun convertFromTxJson(json: String): Payment<LavegoTransactionData> =
        try {
            withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                createTxJsonAdapter().fromJson(json)?.toDomain()
            }.errorIfNull("data could not be converted to transaction data: $json")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.v(e, "data could not be converted to transaction data: %s", json)
            Payment.Error("data could not be converted to transaction data: $json")
        }

    private fun createLoginJsonAdapter(): JsonAdapter<LoginDataDto> =
        moshi.adapter(LoginDataDto::class.java)

    suspend fun convertFromLoginJson(json: String): Payment<LavegoLoginData> =
        try {
            withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                createLoginJsonAdapter().fromJson(json)?.toDomain()
            }.errorIfNull("data could not be converted to login data: $json")
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.v(e, "data could not be converted to login data: %s", json)
            Payment.Error("data could not be converted to login data: $json")
        }

    private fun TransactionDataDto.toDomain(): LavegoTransactionData =
        LavegoTransactionData(
            additionalText = additionalText,
            aid = aid,
            amount = BigInteger.valueOf(amount),
            cardName = cardName,
            cardSeqNumber = cardSeqNumber,
            cardType = cardType,
            chipData = chipData,
            data = data,
            date = date,
            expiry = expiry,
            receiptNo = receiptNo,
            resultCode = resultCode,
            resultText = resultText,
            singleAmounts = singleAmounts,
            tags = tags,
            tid = tid,
            time = time,
            trace = trace,
            traceOrig = traceOrig,
            track1 = track1,
            track2 = track2,
            track3 = track3,
            vu = vu,
        )

    private fun LoginDataDto.toDomain(): LavegoLoginData = LavegoLoginData(responseApdu)
}

@Keep
@JsonClass(generateAdapter = true)
internal data class TransactionDataDto(
    @Json(name = "additional_text") val additionalText: String,
    @Json(name = "aid") val aid: String,
    @Json(name = "amount") val amount: Long,
    @Json(name = "card_name") val cardName: String,
    @Json(name = "card_seq_no") val cardSeqNumber: Int,
    @Json(name = "card_type") val cardType: Int,
    @Json(name = "chip_data") val chipData: String,
    @Json(name = "data") val data: String,
    @Json(name = "date") val date: String,
    @Json(name = "expiry") val expiry: String,
    @Json(name = "receipt_no") val receiptNo: Int,
    @Json(name = "resultCode") val resultCode: Int,
    @Json(name = "resultText") val resultText: String,
    @Json(name = "single_amounts") val singleAmounts: String,
    @Json(name = "tags") val tags: Map<String, String>?,
    @Json(name = "tid") val tid: String,
    @Json(name = "time") val time: String,
    @Json(name = "trace") val trace: String,
    @Json(name = "trace_orig") val traceOrig: String,
    @Json(name = "track1") val track1: String,
    @Json(name = "track2") val track2: String,
    @Json(name = "track3") val track3: String,
    @Json(name = "vu") val vu: String,
)

@Keep
@JsonClass(generateAdapter = true)
internal data class LoginDataDto(
    @Json(name = "response_apdu") val responseApdu: String,
)
