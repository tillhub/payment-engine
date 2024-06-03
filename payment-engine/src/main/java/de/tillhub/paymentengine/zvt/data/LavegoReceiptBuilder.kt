package de.tillhub.paymentengine.zvt.data

internal class LavegoReceiptBuilder(
    startWithMerchant: Boolean = true
) {

    private val customerReceiptBuilder = StringBuilder()
    private val merchantReceiptBuilder = StringBuilder()

    private var isMerchant: Boolean = startWithMerchant

    val customerReceipt: String
        get() = customerReceiptBuilder.toString()

    val merchantReceipt: String
        get() = merchantReceiptBuilder.toString()

    fun addLine(line: String) {
        if (line.isEmpty()) {
            isMerchant = !isMerchant
            return
        }

        val currentBuilder = if (isMerchant) merchantReceiptBuilder else customerReceiptBuilder

        currentBuilder.append(line)
        currentBuilder.append("\n")
    }

    fun addBlock(block: String) {
        val currentBuilder = if (isMerchant) merchantReceiptBuilder else customerReceiptBuilder

        currentBuilder.append(block)

        isMerchant = !isMerchant
    }
}
