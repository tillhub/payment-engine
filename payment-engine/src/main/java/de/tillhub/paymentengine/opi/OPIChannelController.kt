package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.TerminalConfigImpl
import de.tillhub.paymentengine.helper.safeLet
import de.tillhub.paymentengine.opi.communication.OPIChannel0
import de.tillhub.paymentengine.opi.communication.OPIChannel1
import de.tillhub.paymentengine.opi.communication.OPIChannelFactory
import de.tillhub.paymentengine.opi.data.CardServiceRequest
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.PosData
import de.tillhub.paymentengine.opi.data.ServiceRequestType
import de.tillhub.paymentengine.opi.data.TotalAmount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.util.UUID

interface OPIChannelController {
    fun init(terminal: Terminal.OPI)

    suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ): Flow<TerminalOperationStatus>

    // TODO implement other methods
}

class OPIChannelControllerImpl(
    private val converterFactory: ConverterFactory = ConverterFactory(),
    private val channelFactory: OPIChannelFactory = OPIChannelFactory(),
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : OPIChannelController {

    private lateinit var terminal: Terminal.OPI
    private lateinit var channel0: OPIChannel0
    private lateinit var channel1: OPIChannel1

    override fun init(terminal: Terminal.OPI) {
        this.terminal = terminal

        channel0 = channelFactory.newOPIChannel0(terminal.ipAddress, terminal.port)
        channel1 = channelFactory.newOPIChannel1(terminal.port2)
    }

    override suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ): Flow<TerminalOperationStatus> = flow {
        emit(TerminalOperationStatus.Pending.Payment(amount, currency))

        if (this@OPIChannelControllerImpl::terminal.isInitialized &&
            this@OPIChannelControllerImpl::channel0.isInitialized &&
            this@OPIChannelControllerImpl::channel1.isInitialized
            ) {
            channel0.open()
            channel1.open()

            channel0.setOnError { err, message ->
                // TODO
                emit(TerminalOperationStatus.Error.OPI(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = ""
                ))
            }

            channel1.setOnError { err, message ->
                // TODO
                emit(TerminalOperationStatus.Error.OPI(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = ""
                ))
            }
            channel1.setOnMessage { requestXml ->

            }

            val requestConverter = converterFactory.newRequestConverter<CardServiceRequest>()
            val responseConverter = converterFactory.newResponseConverter(
                clazz = CardServiceResponse::class.java
            )

            val payload = CardServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = UUID.randomUUID().toString(),
                requestType = ServiceRequestType.CARD_PAYMENT.value,
                workstationID = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow()),
                totalAmount = TotalAmount(
                    value = amount,
                    currency = currency.value
                )
            )

            val xml = requestConverter.convert(payload)
            channel0.sendMessage(xml) { responseXml ->
                val response = responseConverter.convert(responseXml)

                if(response?.overallResult == "Success")

                emit(TerminalOperationStatus.Success.OPI(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = responseXml
                ))
            }

        } else {
            // TODO emit NOT INITED
            emit(TerminalOperationStatus.Error.OPI(
                date = terminalConfig.timeNow(),
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ""
            ))
        }
    }
}