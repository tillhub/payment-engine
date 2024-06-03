package de.tillhub.paymentengine.data

import android.os.Parcelable
import de.lavego.ISO4217.ISOCurrency
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class ISOAlphaCurrency(val value: String) : Parcelable {
    init {
        require(
            ISOCurrency.entries.any { this.value == it.alpha() }
        ) {
            "Currency must be part of ISO 4217 currency codes, was $value"
        }
    }
}