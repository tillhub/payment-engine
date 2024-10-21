package de.tillhub.paymentengine.zvt.ui

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.lavego.ISO4217
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.api.Bmp
import de.lavego.zvt.api.Commons
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.databinding.ActivityCardPaymentPartialRefundBinding
import de.tillhub.paymentengine.helper.viewBinding
import java.math.BigDecimal

internal class CardPaymentPartialRefundActivity : CardTerminalActivity() {

    companion object {
        private const val TAG = "CardPaymentPartialRefundActivity"
        private const val AMOUNT_BYTE_COUNT: Int = 6
    }

    private val binding by viewBinding(ActivityCardPaymentPartialRefundBinding::inflate)

    private val amount: BigDecimal by lazy {
        intent.extras?.let {
            BundleCompat.getSerializable(it, ExtraKeys.EXTRA_AMOUNT, BigDecimal::class.java)
                ?: throw IllegalArgumentException("$TAG: Argument amount is missing")
        } ?: throw IllegalArgumentException("$TAG: Extras are missing")
    }
    private val currency: ISOAlphaCurrency by lazy {
        intent.extras?.let {
            BundleCompat.getParcelable(it, ExtraKeys.EXTRA_CURRENCY, ISOAlphaCurrency::class.java)
                ?: throw IllegalArgumentException("$TAG: Argument currency is missing")
        } ?: throw IllegalArgumentException("$TAG: Extras are missing")
    }

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
        binding.nexoError.isGone = true
        binding.instructions.isGone = true
    }

    override fun showInstructions() {
        binding.instructions.isVisible = true
        binding.loader.isGone = true
        binding.nexoError.isGone = true
    }

    override fun showIntermediateStatus(status: String) {
        binding.statusMessage.text = status
    }

    override fun showCancel() {
        binding.buttonCancel.isVisible = true
    }

    override fun startOperation() {
        analytics?.logOperation(
            "Operation: PARTIAL_REFUND(" +
                "amount: $amount, " +
                "currency: $currency)" +
                "\n$config"
        )

        doPartialRefund(
            amount = amount,
            currency = currency
        )
    }

    /**
     * This method is called to initiate a card transaction partial refund
     *
     * @param amount the amount to be refunded
     */
    private fun doPartialRefund(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val partialRefund = Apdu(Commons.Command.CMD_0631).apply {
            val password = config.saleConfig.pin
            val currencyAsTwoByteHex = ISO4217.ISOCurrency.byAlpha(currency.value).codeAsTwoByteHex()
            add(Commons.StringNumberToBCD(password, PASSWORD_BYTE_COUNT))
            add(Bmp(0x04.toByte(), Commons.NumberToBCD(amount.toLong(), AMOUNT_BYTE_COUNT)))
            add(Bmp(0x49.toByte(), Commons.StringNumberToBCD(currencyAsTwoByteHex, CC_BYTE_COUNT)))
        }

        doCustom(partialRefund.apdu())
    }
}
