package de.tillhub.paymentengine.zvt

import de.tillhub.paymentengine.data.LavegoReceiptBuilder
import de.tillhub.paymentengine.data.LavegoTransactionDataConverter
import kotlinx.coroutines.CoroutineScope

class ZvtManagerImpl(
    private val applicationScope: CoroutineScope,
    private val lavegoTransactionDataConverter: LavegoTransactionDataConverter
) : ZvtManager {

    private var lastReceipt: LavegoReceiptBuilder? = null
    private var lastData: String? = null

    override fun onRawData(hex: String?) = Unit

    override fun onStatus(status: String?) {
        lastData = status
    }

    override fun onIntermediateStatus(status: String?) = Unit

    override fun onCompletion(completion: String?) {
        TODO("Not yet implemented")
    }

    override fun onReceipt(receipt: String) {
        if (lastReceipt == null) {
            lastReceipt = LavegoReceiptBuilder()
        }
        if (receipt.contains('\n')) {
            lastReceipt!!.addBlock(receipt)
        } else {
            lastReceipt!!.addLine(receipt)
        }
    }

    override fun onError(error: String?) {
        TODO("Not yet implemented")
    }

    override fun onSocketConnected(connected: Boolean) {
        TODO("Not yet implemented")
    }
}