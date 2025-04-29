package de.tillhub.paymentengine.softpay.data

import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class SoftpayTerminal(
    override val id: String = DEFAULT_SOFTPAY_ID,
    val config: SoftpayConfig
) : Terminal.External(id) {

    override fun connectIntent(input: Terminal): Intent =
        Intent(INTENT_ACTION_CONNECT).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input)
        }

    override fun paymentIntent(input: PaymentRequest): Intent =
        Intent(INTENT_ACTION_PAYMENT).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount + input.tip)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
        }

    override fun refundIntent(input: RefundRequest): Intent = Intent(INTENT_ACTION_REFUND).apply {
        putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
        putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
        putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
    }

    override fun reversalIntent(input: ReversalRequest): Intent =
        Intent(INTENT_ACTION_REVERSAL).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
        }

    override fun reconciliationIntent(input: Terminal): Intent =
        Intent(INTENT_ACTION_RECONCILIATION).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input)
        }

    override fun toString() = "SoftpayTerminal(" +
            "id=$id, " +
            "saleConfig=$saleConfig, " +
            "config=$config, " +
            ")"

    override fun equals(other: Any?) = other is SoftpayTerminal &&
            id == other.id &&
            saleConfig == other.saleConfig &&
            config == other.config

    override fun hashCode() = Objects.hash(
        id,
        saleConfig,
        config
    )

    companion object {
        private const val INTENT_ACTION_CONNECT = "de.tillhub.paymentengine.softpay.ACTION_CONNECT"
        private const val INTENT_ACTION_PAYMENT = "de.tillhub.paymentengine.softpay.ACTION_PAYMENT"
        private const val INTENT_ACTION_REFUND = "de.tillhub.paymentengine.softpay.ACTION_REFUND"
        private const val INTENT_ACTION_REVERSAL = "de.tillhub.paymentengine.softpay.ACTION_REVERSAL"
        private const val INTENT_ACTION_RECONCILIATION = "de.tillhub.paymentengine.softpay.ACTION_RECONCILIATION"

        private const val DEFAULT_SOFTPAY_ID = "Default:SOFTPAY"
        const val TYPE = "SOFTPAY"
    }
}