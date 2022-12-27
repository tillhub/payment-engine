package de.tillhub.paymentengine

import de.lavego.sdk.PaymentProtocol
import de.lavego.sdk.SaleConfiguration
import de.lavego.sdk.TransportConfiguration
import de.lavego.zvt.api.Apdu
import de.lavego.zvt.api.Bmp
import de.lavego.zvt.api.Commons
import de.tillhub.paymentengine.data.*
import de.tillhub.paymentengine.di.PaymentTime

class PaymentManagerImpl(
    private val paymentTime: PaymentTime
) : PaymentManager {
    override fun getTransportConfiguration(cardPaymentConfig: CardPaymentConfig): TransportConfiguration =
        TransportConfiguration().apply {
            when (cardPaymentConfig.integrationType) {
                IntegrationType.ZVT -> {
                    paymentProtocol = PaymentProtocol.Zvt
                    host = cardPaymentConfig.ipAddress
                    port = cardPaymentConfig.port
                }
                IntegrationType.NEXO -> {
                    paymentProtocol = PaymentProtocol.Nexo
                    host = CardPaymentConfig.LOCALHOST_IP
                    port = CardPaymentConfig.NEXO_PORT
                }
            }
        }

    override fun getSaleConfiguration(cardSaleConfig: CardSaleConfig): SaleConfiguration {
        return SaleConfiguration().apply {
            applicationName = cardSaleConfig.applicationName
            operatorId = cardSaleConfig.operatorId
            saleId = cardSaleConfig.saleId
            pin = cardSaleConfig.pin
            poiId = cardSaleConfig.poiId
            poiSerialnumber = cardSaleConfig.poiSerialNumber
            trainingMode = cardSaleConfig.trainingMode

            zvtFlags.isoCurrencyRegister(cardSaleConfig.isoCurrencyNumber)
        }
    }

    override fun getSetupProtocol(
        cardPaymentConfig: CardPaymentConfig,
        cardSaleConfig: CardSaleConfig
    ): SetupProtocol {
        return when(val protocol = getTransportConfiguration(cardPaymentConfig).paymentProtocol) {
            PaymentProtocol.Nexo -> SetupProtocol.Nexo
            PaymentProtocol.Zvt -> SetupProtocol.ZVT(
                Apdu(Commons.Command.CMD_0600).apply {
                    val password = getSaleConfiguration(cardSaleConfig).pin
                    val currency = getSaleConfiguration(cardSaleConfig).zvtFlags.isoCurrencyRegister()

                    add(Commons.StringNumberToBCD(password,PASSWORD_BYTE_COUNT))
                    add(TERMINAL_CONFIG_BYTE)
                    add(Commons.StringNumberToBCD(currency, CC_BYTE_COUNT))
                    add(Bmp(REGISTER_TLV_CONTAINER_STRING))
                }
            )
            else -> throw IllegalArgumentException("Not supported payment protocol: $protocol")
        }
    }

    companion object {
        private const val PASSWORD_BYTE_COUNT: Int = 3
        private const val TERMINAL_CONFIG_BYTE: Byte = 0b11000110.toByte()
        private const val CC_BYTE_COUNT: Int = 2
        private const val REGISTER_TLV_CONTAINER_STRING = "06 0C 12 01 30 27 03 14 01 FE 40 02 B0 B0"
    }
}