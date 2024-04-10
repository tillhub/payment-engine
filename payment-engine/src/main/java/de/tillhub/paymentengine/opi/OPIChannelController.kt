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
import de.tillhub.paymentengine.opi.data.DeviceRequestType
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.DeviceType
import de.tillhub.paymentengine.opi.data.TotalAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.UUID

interface OPIChannelController {

    val operationState: StateFlow<OPIOperationStatus>

    fun init(terminal: Terminal.OPI)
    fun close()

    suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    )
    // TODO implement other methods
}

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class OPIChannelControllerImpl(
    private val converterFactory: ConverterFactory = ConverterFactory(),
    private val channelFactory: OPIChannelFactory = OPIChannelFactory(),
    private val terminalConfig: TerminalConfig = TerminalConfigImpl()
) : OPIChannelController {

    private lateinit var terminal: Terminal.OPI
    private lateinit var channel0: OPIChannel0
    private lateinit var channel1: OPIChannel1

    private val _operationState = MutableStateFlow<OPIOperationStatus>(OPIOperationStatus.Idle)

    private val initialized: Boolean
        get() = this@OPIChannelControllerImpl::terminal.isInitialized &&
                this@OPIChannelControllerImpl::channel0.isInitialized &&
                this@OPIChannelControllerImpl::channel1.isInitialized

    override val operationState: StateFlow<OPIOperationStatus>
        get() = _operationState

    override fun init(terminal: Terminal.OPI) {
        this.terminal = terminal

        if (initialized) {
            finishOperation()

            _operationState.value = OPIOperationStatus.Idle
        }

        channel0 = channelFactory.newOPIChannel0(terminal.ipAddress, terminal.port)
        channel1 = channelFactory.newOPIChannel1(terminal.port2)
    }

    @Suppress("LongMethod")
    override suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ) {
        if (_operationState.value !is OPIOperationStatus.Idle) return

        _operationState.value = OPIOperationStatus.Pending(terminalConfig.timeNow())

        if (initialized) {
            channel0.open()
            channel0.setOnError { err, message ->
                // TODO
                _operationState.value = OPIOperationStatus.Error.Communication(message)
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

            val xml = try {
                requestConverter.convert(payload)
            } catch (e: Exception) {
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 0 request object could not be converted to XML.",
                    error = e
                )
                return
            }

            while (!channel0.isConnected) {
                delay(CONNECTION_WAIT_DELAY)
            }

            channel0.sendMessage(xml) { responseXml ->
                val response = try {
                    responseConverter.convert(responseXml)
                } catch (e: Exception) {
                    _operationState.value = OPIOperationStatus.Error.DataHandling(
                        message = "Channel 0 response XML could not be parsed.",
                        error = e
                    )
                    return@sendMessage
                }

                val customerReceipt = (_operationState.value as? OPIOperationStatus.Pending)
                    ?.customerReceipt.orEmpty()
                val merchantReceipt = (_operationState.value as? OPIOperationStatus.Pending)
                    ?.merchantReceipt.orEmpty()

                if (response.overallResult == OverallResult.SUCCESS.value) {
                    _operationState.value = OPIOperationStatus.Result.Success(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = null
                    )
                } else {
                    _operationState.value = OPIOperationStatus.Result.Error(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = null
                    )
                }

                finishOperation()
            }
        } else {
            _operationState.value = OPIOperationStatus.Error.NotInitialised
        }
    }

    override fun close() {
        if (initialized) {
            finishOperation()

            _operationState.value = OPIOperationStatus.Idle
        }
    }

    private fun finishOperation() {
        channel0.close()
        channel1.close()
    }

    @Suppress("LongMethod")
    private fun handleChannel1Communication() {
        channel1.open()

        val requestConverter = converterFactory.newStringToDtoConverter(
            clazz = DeviceRequest::class.java
        )
        val responseConverter = converterFactory.newDtoToStringConverter<DeviceResponse>()

        channel1.setOnError { err, message ->
            _operationState.value = OPIOperationStatus.Error.Communication(message)
        }

        channel1.setOnMessage { requestXml ->
            val request = try {
                requestConverter.convert(requestXml)
            } catch (e: Exception) {
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 1 request XML could not be parsed.",
                    error = e
                )
                return@setOnMessage
            }
            if (request.requestType == DeviceRequestType.OUTPUT.value) {
                request.output?.forEach { output ->
                    val target = DeviceType.entries.find { it.value == output.outDeviceTarget }
                        ?: DeviceType.UNKNOWN

                    val currentState = (_operationState.value as? OPIOperationStatus.Pending)
                        ?: OPIOperationStatus.Pending(terminalConfig.timeNow())

                    _operationState.value = when (target) {
                        DeviceType.CASHIER_DISPLAY,
                        DeviceType.CUSTOMER_DISPLAY -> currentState.copy(
                            messageLines = output.textLines
                                ?.sortedBy { it.row ?: 0 }
                                ?.mapNotNull {
                                    it.value
                                }.orEmpty()
                        )
                        DeviceType.PRINTER -> currentState.copy(
                            merchantReceipt = StringBuilder().apply {
                                output.textLines?.forEach { appendLine(it.value) }
                            }.toString()
                        )
                        DeviceType.PRINTER_RECEIPT -> currentState.copy(
                            customerReceipt = StringBuilder().apply {
                                output.textLines?.forEach { appendLine(it.value) }
                            }.toString()
                        )
                        else -> currentState // TODO> Not handled yet
                    }
                }
            } else {
                // TODO> handle input requests
            }

            // Sending response
            val payload = try {
                responseConverter.convert(DeviceResponse(
                    applicationSender = terminal.saleConfig.applicationName,
                    popId = request.popId,
                    requestId = request.requestId,
                    requestType = request.requestType,
                    workstationID = request.requestId,
                    overallResult = OverallResult.SUCCESS.value,
                    output = request.output?.map {
                        it.copy(
                            outResult = OverallResult.SUCCESS.value,
                            textLines = null
                        )
                    }
                ))
            } catch (e: Exception) {
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 1 response object could not be converted to XML.",
                    error = e
                )
                return@setOnMessage
            }

            channel1.sendMessage(payload)
        }
    }

    companion object {
        private const val CONNECTION_WAIT_DELAY = 100L
    }
}