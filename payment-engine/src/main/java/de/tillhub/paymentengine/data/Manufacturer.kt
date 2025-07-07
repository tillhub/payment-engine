package de.tillhub.paymentengine.data

internal enum class Manufacturer(open val value: String) {
    PAX("PAX"),
    OTHER("OTHER");

    companion object {
        fun get(): Manufacturer =
            entries.firstOrNull {
                it.value == android.os.Build.MANUFACTURER
            } ?: OTHER
        fun matches(value: Manufacturer): Boolean = get() == value
    }
}