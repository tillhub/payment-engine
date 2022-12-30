package de.tillhub.paymentengine


import de.tillhub.paymentengine.data.CardPaymentConfig
import de.tillhub.paymentengine.data.IntegrationType
import kotlinx.coroutines.flow.StateFlow

interface CardPaymentConfigRepository {
    val configStateFlow: StateFlow<CardPaymentConfig>
    val config: CardPaymentConfig

    suspend fun setIpAddress(address: String)
    suspend fun setProtocol(protocol: IntegrationType)
    suspend fun setPort(port: Int)
}
