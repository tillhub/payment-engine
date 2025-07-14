package de.tillhub.paymentengine.zvt.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class LavegoLoginData(
    val responseApdu: String,
) : Parcelable
