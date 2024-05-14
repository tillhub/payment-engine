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
import de.tillhub.paymentengine.opi.data.ServiceRequestType
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.DeviceRequest
import de.tillhub.paymentengine.opi.data.DeviceRequestType
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.DeviceType
import de.tillhub.paymentengine.opi.data.DtoToStringConverter
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.data.OriginalTransaction
import de.tillhub.paymentengine.opi.data.OverallResult
import de.tillhub.paymentengine.opi.data.PosData
import de.tillhub.paymentengine.opi.data.ServiceRequest
import de.tillhub.paymentengine.opi.data.ServiceResponse
import de.tillhub.paymentengine.opi.data.StringToDtoConverter
import de.tillhub.paymentengine.opi.data.TotalAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal
import kotlin.random.Random

interface OPIChannelController {

    val operationState: StateFlow<OPIOperationStatus>

    fun init(terminal: Terminal.OPI)
    fun close()

    suspend fun login()

    suspend fun initiatePaymentReversal(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    )
    suspend fun initiatePaymentReversal(stan: String)
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

    override fun close() {
        if (initialized) {
            finishOperation()

            _operationState.value = OPIOperationStatus.Idle
        }
    }

    override suspend fun login() {
        // If the state is not Idle, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.Idle) return

        _operationState.value = OPIOperationStatus.Pending.Login

        // checks if the controller is initialized
        if (initialized) {
            val requestConverter = converterFactory.newDtoToStringConverter<ServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = ServiceResponse::class.java
            )

            val payload = ServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = generateRequestId(),
                requestType = ServiceRequestType.LOGIN.value,
                workstationID = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow().toISOString()),
            )

            // setup C0 communication
            handleChannel0Communication(payload, requestConverter) { responseXml ->
                val response = try {
                    responseConverter.convert(responseXml)
                } catch (e: Exception) {
                    // In case of an exception when converting the XML to the DTO we
                    // set the state to `Error.DataHandling`.
                    _operationState.value = OPIOperationStatus.Error.DataHandling(
                        message = "Channel 0 response XML could not be parsed.",
                        error = e
                    )
                    return@handleChannel0Communication
                }

                _operationState.value = when (OverallResult.find(response.overallResult)) {
                    OverallResult.SUCCESS -> OPIOperationStatus.LoggedIn

                    else -> OPIOperationStatus.Result.Error(
                        date = terminalConfig.timeNow(),
                        customerReceipt = "",
                        merchantReceipt = "",
                        rawData = responseXml,
                        data = null
                    )
                }

                // as per protocol, both channels are closed after C0 response
                finishOperation()
            }
        } else {
            // in case the controller is not initialized set the state to `Error.NotInitialised`
            _operationState.value = OPIOperationStatus.Error.NotInitialised
        }
    }

    override suspend fun initiatePaymentReversal(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ) {
        // If the state is not LoggedIn, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.LoggedIn) return

        _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

        // checks if the controller is initialized
        if (initialized) {
            // setup C1 communication, it has to be setup before C0,
            // because once C0 request is sent, C1 needs to handle the intermediate communication
            handleChannel1Communication()

            val requestConverter = converterFactory.newDtoToStringConverter<CardServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = CardServiceResponse::class.java
            )

            val payload = CardServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = generateRequestId(),
                requestType = ServiceRequestType.CARD_PAYMENT.value,
                workstationID = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow().toISOString()),
                totalAmount = TotalAmount(
                    value = amount.setScale(2),
                    currency = currency.value
                )
            )

            // setup C0 communication
            handleChannel0Communication(payload, requestConverter) { responseXml ->
                val response = try {
                    responseConverter.convert(responseXml)
                } catch (e: Exception) {
                    // In case of an exception when converting the XML to the DTO we
                    // set the state to `Error.DataHandling`.
                    _operationState.value = OPIOperationStatus.Error.DataHandling(
                        message = "Channel 0 response XML could not be parsed.",
                        error = e
                    )
                    return@handleChannel0Communication
                }

                val customerReceipt =
                    (_operationState.value as? OPIOperationStatus.Pending.Operation)
                    ?.customerReceipt.orEmpty()
                val merchantReceipt =
                    (_operationState.value as? OPIOperationStatus.Pending.Operation)
                    ?.merchantReceipt.orEmpty()

                _operationState.value = when (OverallResult.find(response.overallResult)) {
                    OverallResult.SUCCESS -> OPIOperationStatus.Result.Success(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = response
                    )
                    else -> OPIOperationStatus.Result.Error(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = response
                    )
                }

                // as per protocol, both channels are closed after C0 response
                finishOperation()
            }
        } else {
            // in case the controller is not initialized set the state to `Error.NotInitialised`
            _operationState.value = OPIOperationStatus.Error.NotInitialised
        }
    }

    override suspend fun initiatePaymentReversal(stan: String) {
        // If the state is not LoggedIn, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.LoggedIn) return

        _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

        // checks if the controller is initialized
        if (initialized) {
            // setup C1 communication, it has to be setup before C0,
            // because once C0 request is sent, C1 needs to handle the intermediate communication
            handleChannel1Communication()

            val requestConverter = converterFactory.newDtoToStringConverter<CardServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = CardServiceResponse::class.java
            )

            val payload = CardServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = generateRequestId(),
                requestType = ServiceRequestType.PAYMENT_REVERSAL.value,
                workstationID = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow().toISOString()),
                originalTransaction = OriginalTransaction(stan)
            )

            // setup C0 communication
            handleChannel0Communication(payload, requestConverter) { responseXml ->
                val response = try {
                    responseConverter.convert(responseXml)
                } catch (e: Exception) {
                    // In case of an exception when converting the XML to the DTO we
                    // set the state to `Error.DataHandling`.
                    _operationState.value = OPIOperationStatus.Error.DataHandling(
                        message = "Channel 0 response XML could not be parsed.",
                        error = e
                    )
                    return@handleChannel0Communication
                }

                val customerReceipt =
                    (_operationState.value as? OPIOperationStatus.Pending.Operation)
                        ?.customerReceipt.orEmpty()
                val merchantReceipt =
                    (_operationState.value as? OPIOperationStatus.Pending.Operation)
                        ?.merchantReceipt.orEmpty()

                _operationState.value = when (OverallResult.find(response.overallResult)) {
                    OverallResult.SUCCESS -> OPIOperationStatus.Result.Success(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = response
                    )
                    else -> OPIOperationStatus.Result.Error(
                        date = terminalConfig.timeNow(),
                        customerReceipt = customerReceipt,
                        merchantReceipt = merchantReceipt,
                        rawData = responseXml,
                        data = response
                    )
                }

                // as per protocol, both channels are closed after C0 response
                finishOperation()
            }
        } else {
            // in case the controller is not initialized set the state to `Error.NotInitialised`
            _operationState.value = OPIOperationStatus.Error.NotInitialised
        }
    }

    private fun finishOperation() {
        channel0.close()
        channel1.close()
    }

    /**
     * This method sets up communication on OPI channel 1,
     * it opens the socket, creates the data converters and
     * sets up the error and message listeners.
     */
    private fun handleChannel1Communication() {
        val requestConverter = converterFactory.newStringToDtoConverter(
            clazz = DeviceRequest::class.java
        )
        val responseConverter = converterFactory.newDtoToStringConverter<DeviceResponse>()

        channel1.setOnError { err, message ->
            _operationState.value = OPIOperationStatus.Error.Communication(message)
        }
        channel1.setOnMessage(getC1MessageHandler(requestConverter, responseConverter))

        channel1.open()
    }

    /**
     * This method returns the message listener for OPI channel 0.
     * the lambda converts the received XML message to the DTO,
     * then based on the message it sets the controller state and
     * answers according to the OPI protocol.
     */
    private fun getC1MessageHandler(
        requestConverter: StringToDtoConverter<DeviceRequest>,
        responseConverter: DtoToStringConverter<DeviceResponse>
    ): (String) -> Unit = { requestXml ->
        try {
            requestConverter.convert(requestXml)
        } catch (e: Exception) {
            // In case of an exception when converting the XML to the DTO we
            // set the state to `Error.DataHandling`.
            _operationState.value = OPIOperationStatus.Error.DataHandling(
                message = "Channel 1 request XML could not be parsed.",
                error = e
            )
            null
        }?.let { request ->
            when (DeviceRequestType.find(request.requestType)) {
                DeviceRequestType.OUTPUT -> request.output?.forEach { output ->
                    val currentState =
                        (_operationState.value as? OPIOperationStatus.Pending.Operation)
                        ?: OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

                    // For every Output in the C1 request we update the Pending state.
                    _operationState.value = when (DeviceType.find(output.outDeviceTarget)) {
                        // CASHIER_DISPLAY/CUSTOMER_DISPLAY are added to `messageLines` to be displayed on the UI
                        DeviceType.CASHIER_DISPLAY,
                        DeviceType.CUSTOMER_DISPLAY -> currentState.copy(
                            messageLines = output.textLines
                                ?.sortedBy { it.row ?: 0 }
                                ?.mapNotNull {
                                    it.value
                                }.orEmpty()
                        )

                        // PRINTER entries are build into merchantReceipt
                        DeviceType.PRINTER -> currentState.copy(
                            merchantReceipt = StringBuilder().apply {
                                output.textLines?.forEach {
                                    appendLine(it.value.orEmpty())
                                }
                            }.toString()
                        )

                        // PRINTER_RECEIPT entries are build into customerReceipt
                        DeviceType.PRINTER_RECEIPT -> currentState.copy(
                            customerReceipt = StringBuilder().apply {
                                output.textLines?.forEach {
                                    appendLine(it.value.orEmpty())
                                }
                            }.toString()
                        )

                        else -> currentState // TODO> Not handled yet
                    }
                }
                DeviceRequestType.INPUT -> {
                    // TODO> handle input requests
                }
                DeviceRequestType.UNKNOWN -> Unit
            }

            // Sending response
            try {
                responseConverter.convert(DeviceResponse(
                    applicationSender = terminal.saleConfig.applicationName,
                    popId = terminal.saleConfig.poiId,
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
                // In case of an exception when converting the DTO to XML we
                // set the state to `Error.DataHandling`.
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 1 response object could not be converted to XML.",
                    error = e
                )
                null
            }?.let { channel1.sendMessage(it) }
        }
    }

    /**
     * This method sets up communication on OPI channel 0,
     * it opens the socket, sets up the error listener, converts to payload,
     * and sends the XML payload.
     */
    private suspend fun <T> handleChannel0Communication(
        payload: T,
        requestConverter: DtoToStringConverter<T>,
        onResponse: (String) -> Unit
    ) {
        channel0.setOnError { err, message ->
            // TODO
            _operationState.value = OPIOperationStatus.Error.Communication(message)
        }

        channel0.open()

        val xml = try {
            requestConverter.convert(payload)
        } catch (e: Exception) {
            // In case of an exception when converting the TDO to XML we
            // set the state to `Error.DataHandling`.
            _operationState.value = OPIOperationStatus.Error.DataHandling(
                message = "Channel 0 request object could not be converted to XML.",
                error = e
            )
            return
        }

        // here the app waits for the channel 0 socket to connect to the terminal.
        while (!channel0.isConnected) {
            delay(CONNECTION_WAIT_DELAY)
        }

        channel0.sendMessage(xml, onResponse)
    }

    private fun generateRequestId(): String {
        val chars = "1234567890"
        val idBuilder = StringBuilder()
        while (idBuilder.length < REQUEST_ID_LENGTH) {
            idBuilder.append(chars[(Random.nextFloat() * chars.length).toInt()])
        }
        return idBuilder.toString()
    }

    companion object {
        private const val CONNECTION_WAIT_DELAY = 100L
        private const val REQUEST_ID_LENGTH = 8
    }
}