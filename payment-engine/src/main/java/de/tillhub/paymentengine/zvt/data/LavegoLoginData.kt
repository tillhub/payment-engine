package de.tillhub.paymentengine.zvt.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Suppress("LongParameterList")
@Parcelize
internal data class LavegoLoginData(
    val responseApdu: String,
) : Parcelable
