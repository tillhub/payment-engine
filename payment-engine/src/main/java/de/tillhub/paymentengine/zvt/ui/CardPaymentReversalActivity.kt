package de.tillhub.paymentengine.zvt.ui

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.api.Bmp
import de.lavego.zvt.api.Commons
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.databinding.ActivityCardPaymentReversalBinding
import de.tillhub.paymentengine.helper.viewBinding

internal class CardPaymentReversalActivity : CardTerminalActivity() {

    companion object {
        private const val TAG = "CardPaymentReversalActivity"
        private const val RECEIPT_NO_BYTE_COUNT: Int = 2
    }

    private val receiptNo: String by lazy {
        intent.getStringExtra(ExtraKeys.EXTRA_RECEIPT_NO)
            ?: throw IllegalArgumentException("$TAG: Argument receipt number is missing")
    }

    private val binding by viewBinding(ActivityCardPaymentReversalBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonCancel.setOnClickListener {
            doAbortOperation()
            finish()
        }
    }

    override fun showLoader() {
        binding.loader.isVisible = true
        binding.instructions.isGone = true
        binding.nexoError.isGone = true
    }

    override fun showInstructions() {
        binding.instructions.isVisible = true
        binding.loader.isGone = true
        binding.nexoError.isGone = true
    }

    override fun showIntermediateStatus(status: String) {
        binding.statusMessage.text = status
    }

    override fun startOperation() {
        viewModel.parseTransactionNumber(receiptNo).onSuccess {
            analytics?.logOperation(
                "Operation: CARD_PAYMENT_REVERSAL(" +
                    "receiptNo: $receiptNo)" +
                    "\n$config"
            )
            doCancellation(it)
        }
    }

    override fun showCancel() {
        binding.buttonCancel.isVisible = true
    }

    /**
     * This method is called to initiate a card transaction reversal
     *
     * @param receiptNo the receipt number of the transaction we want to reverse
     */
    private fun doCancellation(receiptNo: Long) {
        val cancellation = Apdu(Commons.Command.CMD_0630).apply {
            val password = config.saleConfig.pin
            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(Bmp(0x87.toByte(), Commons.NumberToBCD(receiptNo, RECEIPT_NO_BYTE_COUNT)))
        }

        doCustom(cancellation.apdu())
    }
}
