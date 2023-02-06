package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.CardSaleConfig
import kotlinx.coroutines.flow.Flow

interface CardSaleConfigRepository {
    val configFlow: Flow<CardSaleConfig>
    fun config(): CardSaleConfig

    suspend fun setPoiId(poiId: String)
    suspend fun setPoiSerial(poiSerial: String)
    suspend fun setTerminalPin(pin: String)
}
