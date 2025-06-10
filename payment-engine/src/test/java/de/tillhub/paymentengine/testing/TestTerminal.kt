package de.tillhub.paymentengine.testing

import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalContract
import io.mockk.mockk
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class TestTerminal(
    override val id: String,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
) : Terminal {

    @IgnoredOnParcel
    override val contract: TerminalContract = mockk(relaxed = true)

    override fun toString() = "Terminal.External(" +
            "id=$id, " +
            "saleConfig=$saleConfig" +
            ")"

    override fun equals(other: Any?) = other is TestTerminal &&
            id == other.id &&
            saleConfig == other.saleConfig

    override fun hashCode() = Objects.hash(
        id,
        saleConfig,
    )
}