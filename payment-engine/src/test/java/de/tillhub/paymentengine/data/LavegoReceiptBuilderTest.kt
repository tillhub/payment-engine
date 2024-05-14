package de.tillhub.paymentengine.data

import de.tillhub.paymentengine.zvt.data.LavegoReceiptBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LavegoReceiptBuilderTest : FunSpec({

    test("LavegoReceiptBuilder by line merchant first") {
        val target = LavegoReceiptBuilder(true)

        target.addLine("merchantReceipt")
        target.addLine("")
        target.addLine("customerReceipt")

        target.customerReceipt shouldBe "customerReceipt\n"
        target.merchantReceipt shouldBe "merchantReceipt\n"
    }

    test("LavegoReceiptBuilder by line customer first") {
        val target = LavegoReceiptBuilder(false)

        target.addLine("customerReceipt")
        target.addLine("")
        target.addLine("merchantReceipt")

        target.customerReceipt shouldBe "customerReceipt\n"
        target.merchantReceipt shouldBe "merchantReceipt\n"
    }

    test("LavegoReceiptBuilder by block merchant first") {
        val target = LavegoReceiptBuilder(true)

        target.addBlock("merchantReceipt")
        target.addBlock("customerReceipt")

        target.customerReceipt shouldBe "customerReceipt"
        target.merchantReceipt shouldBe "merchantReceipt"
    }

    test("LavegoReceiptBuilder by block customer first") {
        val target = LavegoReceiptBuilder(false)

        target.addBlock("customerReceipt")
        target.addBlock("merchantReceipt")

        target.customerReceipt shouldBe "customerReceipt"
        target.merchantReceipt shouldBe "merchantReceipt"
    }
})
