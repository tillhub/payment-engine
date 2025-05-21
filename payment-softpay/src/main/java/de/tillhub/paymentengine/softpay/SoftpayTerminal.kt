package de.tillhub.paymentengine.softpay

import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize

@Parcelize
class SoftpayTerminal(
    override val id: String = DEFAULT_SOFTPAY_ID,
) : Terminal.External(id) {

    companion object {
        private const val DEFAULT_SOFTPAY_ID = "Default:SOFTPAY"
    }
}