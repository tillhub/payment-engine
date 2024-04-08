package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.TerminalConfigImpl
import de.tillhub.paymentengine.helper.toISOString
import de.tillhub.paymentengine.opi.communication.OPIChannel0
import de.tillhub.paymentengine.opi.communication.OPIChannel1
import de.tillhub.paymentengine.opi.communication.OPIChannelFactory
import de.tillhub.paymentengine.opi.data.CardServiceRequest
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.DeviceRequest
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.data.OverallResult
import de.tillhub.paymentengine.opi.data.PosData
import de.tillhub.paymentengine.opi.data.CardServiceRequestType
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.TotalAmount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import kotlin.coroutines.CoroutineContext

interface OPIChannelController {
    fun init(terminal: Terminal.OPI)

    suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ): Flow<OPIOperationStatus>

    fun close()
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

    private val initialized: Boolean
        get() = this@OPIChannelControllerImpl::terminal.isInitialized &&
                this@OPIChannelControllerImpl::channel0.isInitialized &&
                this@OPIChannelControllerImpl::channel1.isInitialized

    override fun init(terminal: Terminal.OPI) {
        this.terminal = terminal

        channel0 = channelFactory.newOPIChannel0(terminal.ipAddress, terminal.port)
        channel1 = channelFactory.newOPIChannel1(terminal.port2)
    }

    override suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ): Flow<OPIOperationStatus> = flow {
        emit(OPIOperationStatus.Pending.NoMessage)
        val flowContext = currentCoroutineContext()

        if (initialized) {
            channel0.open()
            channel0.setOnError { err, message ->
                // TODO
                emitSuspended(OPIOperationStatus.Error.Result(
                    date = terminalConfig.timeNow(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = "",
                    data = null
                ), flowContext)
            }

            handleChannel1Communication()

            val requestConverter = converterFactory.newDtoToStringConverter<CardServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = CardServiceResponse::class.java
            )

            val payload = CardServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = UUID.randomUUID().toString(),
                requestType = CardServiceRequestType.CARD_PAYMENT.value,
                workstationID = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow().toISOString()),
                totalAmount = TotalAmount(
                    value = amount.setScale(2),
                    currency = currency.value
                )
            )

            val xml = requestConverter.convert(payload)

            while (!channel0.isConnected) {
                delay(100)
            }

            channel0.sendMessage(xml) { responseXml ->
                val response = responseConverter.convert(responseXml)

                if (response?.overallResult == OverallResult.SUCCESS.value) {
                    emitSuspended(OPIOperationStatus.Success(
                        date = terminalConfig.timeNow(),
                        customerReceipt = "",
                        merchantReceipt = "",
                        rawData = responseXml,
                        data = null
                    ), flowContext)
                } else {
                    // TODO emit errors
                }

                finishOperation()
            }

        } else {
            emit(OPIOperationStatus.Error.NotInitialised)
        }
    }

    override fun close() {
        if (initialized) {
            finishOperation()
        }
    }

    private fun finishOperation() {
        channel0.close()
        channel1.close()
    }

    private suspend fun FlowCollector<OPIOperationStatus>.handleChannel1Communication() {
        channel1.open()

        val requestConverter = converterFactory.newStringToDtoConverter(
            clazz = DeviceRequest::class.java
        )
        val responseConverter = converterFactory.newDtoToStringConverter<DeviceResponse>()

        channel1.setOnError { err, message ->
            // TODO

        }
        channel1.setOnMessage { requestXml ->
            val request = requestConverter.convert(requestXml)

            // TODO
        }
    }

    private fun <T> FlowCollector<T>.emitSuspended(value: T, context: CoroutineContext) =
        CoroutineScope(context).launch {
            emit(value)
        }
}