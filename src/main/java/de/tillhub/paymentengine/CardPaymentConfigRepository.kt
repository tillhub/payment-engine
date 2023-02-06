package de.tillhub.paymentengine

import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.IntegrationType
import kotlinx.coroutines.flow.Flow

interface CardPaymentConfigRepository {
    val configFlow: Flow<CardPaymentConfig>
    fun config(): CardPaymentConfig

    suspend fun setIpAddress(address: String)
    suspend fun setProtocol(protocol: IntegrationType)
    suspend fun setPort(port: Int)
}
