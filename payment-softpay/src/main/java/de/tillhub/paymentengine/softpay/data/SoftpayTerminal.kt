package de.tillhub.paymentengine.softpay.data

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.softpay.ui.connect.SoftpayConnectActivity
import kotlinx.parcelize.IgnoredOnParcel
import java.util.Objects

class SoftpayTerminal(
    id: String = DEFAULT_SOFTPAY_ID,
    val config: SoftpayConfig
) : Terminal.External(id) {
    @IgnoredOnParcel
    override val connectActivity: Class<*> = SoftpayConnectActivity::class.java
    @IgnoredOnParcel
    override val paymentActivity: Class<*>? = null
    @IgnoredOnParcel
    override val refundActivity: Class<*>? = null
    @IgnoredOnParcel
    override val reversalActivity: Class<*>? = null
    @IgnoredOnParcel
    override val reconciliationActivity: Class<*>? = null

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
        private const val DEFAULT_SOFTPAY_ID = "Default:SOFTPAY"
    }
}