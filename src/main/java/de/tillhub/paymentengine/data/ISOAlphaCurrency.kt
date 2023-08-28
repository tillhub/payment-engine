package de.tillhub.paymentengine.data

import de.lavego.ISO4217.ISOCurrency

@JvmInline
value class ISOAlphaCurrency(val value: String) {
    init {
        require(ISOCurrency.values().any {
            this.value == it.alpha()
        }) { "Currency must be part of ISO 4217 currency codes, was $value" }
    }
}