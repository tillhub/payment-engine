package de.tillhub.paymentengine.zvt.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class LavegoTransactionData(
    val additionalText: String,
    val aid: String,
    val amount: BigInteger,
    val cardName: String,
    val cardType: Int,
    val cardSeqNumber: Int,
    val chipData: String,
    val data: String,
    val date: String, // format of this field is "29.10.2020"
    val expiry: String,
    val receiptNo: Int,
    val resultCode: Int,
    val resultText: String,
    val singleAmounts: String,
    val tags: Map<String, String>?,
    val tid: String,
    val time: String, // format of this field is "29.10.2020 22:58:59"
    val trace: String,
    val traceOrig: String,
    val track1: String,
    val track2: String,
    val track3: String,
    val vu: String,
) : Parcelable
