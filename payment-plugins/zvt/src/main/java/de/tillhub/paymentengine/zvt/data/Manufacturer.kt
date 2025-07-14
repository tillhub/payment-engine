package de.tillhub.paymentengine.zvt.data

import android.os.Build

internal enum class Manufacturer(open val value: String) {
    PAX("PAX"),
    OTHER("OTHER");

    companion object {
        fun get(): Manufacturer =
            entries.firstOrNull {
                it.value == Build.MANUFACTURER
            } ?: OTHER
        fun matches(value: Manufacturer): Boolean = get() == value
    }
}