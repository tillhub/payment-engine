package de.tillhub.paymentengine.softpay

import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.IgnoredOnParcel

class SoftpayTerminal(
    id: String = DEFAULT_SOFTPAY_ID,
) : Terminal.External(id) {
    @IgnoredOnParcel
    override val connectActivity: Class<*>? = null
    @IgnoredOnParcel
    override val paymentActivity: Class<*>? = null
    @IgnoredOnParcel
    override val refundActivity: Class<*>? = null
    @IgnoredOnParcel
    override val reversalActivity: Class<*>? = null
    @IgnoredOnParcel
    override val reconciliationActivity: Class<*>? = null

    companion object {
        private const val DEFAULT_SOFTPAY_ID = "Default:SOFTPAY"
    }
}