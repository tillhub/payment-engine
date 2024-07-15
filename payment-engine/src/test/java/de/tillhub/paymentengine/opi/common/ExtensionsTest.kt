package de.tillhub.paymentengine.opi.common

import de.tillhub.paymentengine.data.ISOAlphaCurrency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExtensionsTest : FunSpec({

    test("modifyAmountForOpi") {
        // https://en.wikipedia.org/wiki/ISO_4217 to check decimal places for currency

        val target = 100.0.toBigDecimal()

        // Icelandic krona has 0 decimal places
        val isk = target.modifyAmountForOpi(ISOAlphaCurrency("ISK"))
        // Iraqi dinar has 3 decimal places
        val iqd = target.modifyAmountForOpi(ISOAlphaCurrency("IQD"))
        // Euro has 2 decimal places
        val eur = target.modifyAmountForOpi(ISOAlphaCurrency("EUR"))

        isk shouldBe 100.0.toBigDecimal()
        iqd shouldBe 0.1.toBigDecimal().setScale(4)
        eur shouldBe 1.0.toBigDecimal().setScale(3)
    }
})
