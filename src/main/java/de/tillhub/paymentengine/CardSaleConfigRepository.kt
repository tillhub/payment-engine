package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.CardSaleConfig
import kotlinx.coroutines.flow.StateFlow

interface CardSaleConfigRepository {
    val configStateFlow: StateFlow<CardSaleConfig>
    val config: CardSaleConfig

    suspend fun setPoiId(poiId: String)
    suspend fun setPoiSerial(poiSerial: String)
    suspend fun setTerminalPin(pin: String)
}
