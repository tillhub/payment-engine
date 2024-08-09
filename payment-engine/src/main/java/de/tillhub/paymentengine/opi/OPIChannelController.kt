package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.PaymentEngine
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.TerminalConfigImpl
import de.tillhub.paymentengine.helper.toISOString
import de.tillhub.paymentengine.opi.common.withOPIContext
import de.tillhub.paymentengine.opi.communication.OPIChannel0
import de.tillhub.paymentengine.opi.communication.OPIChannel1
import de.tillhub.paymentengine.opi.communication.OPIChannelFactory
import de.tillhub.paymentengine.opi.data.CardServiceRequest
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.DeviceRequest
import de.tillhub.paymentengine.opi.data.DeviceRequestType
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.DeviceType
import de.tillhub.paymentengine.opi.data.DtoToStringConverter
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.data.OPIResponse
import de.tillhub.paymentengine.opi.data.OriginalTransaction
import de.tillhub.paymentengine.opi.data.Output
import de.tillhub.paymentengine.opi.data.OverallResult
import de.tillhub.paymentengine.opi.data.PosData
import de.tillhub.paymentengine.opi.data.RequestIdFactory
import de.tillhub.paymentengine.opi.data.ServiceRequest
import de.tillhub.paymentengine.opi.data.ServiceRequestType
import de.tillhub.paymentengine.opi.data.ServiceResponse
import de.tillhub.paymentengine.opi.data.StringToDtoConverter
import de.tillhub.paymentengine.opi.data.TotalAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.math.BigDecimal

internal interface OPIChannelController {

    val operationState: StateFlow<OPIOperationStatus>

    fun init(terminal: Terminal.OPI)
    fun close()

    suspend fun login()

    suspend fun initiateCardPayment(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    )
    suspend fun initiatePaymentReversal(stan: String)
    suspend fun initiatePartialRefund(
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    )
    suspend fun initiateReconciliation()
}

@Suppress("TooGenericExceptionCaught", "SwallowedException", "TooManyFunctions")
internal class OPIChannelControllerImpl(
    private val converterFactory: ConverterFactory = ConverterFactory(),
    private val channelFactory: OPIChannelFactory = OPIChannelFactory(),
    private val terminalConfig: TerminalConfig = TerminalConfigImpl(),
    private val requestIdFactory: RequestIdFactory = RequestIdFactory(),
    private val analytics: PaymentAnalytics? = PaymentEngine.getInstance().getAnalytics()
) : OPIChannelController {

    private lateinit var terminal: Terminal.OPI
    private lateinit var channel0: OPIChannel0
    private lateinit var channel1: OPIChannel1

    private val initialized: Boolean
        get() = this@OPIChannelControllerImpl::terminal.isInitialized &&
                this@OPIChannelControllerImpl::channel0.isInitialized &&
                this@OPIChannelControllerImpl::channel1.isInitialized

    private val _operationState = MutableStateFlow<OPIOperationStatus>(OPIOperationStatus.Idle)
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

    @Suppress("LongMethod")
    override suspend fun login() = withOPIContext {
        // If the state is not Idle, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.Idle) return@withOPIContext

        _operationState.value = OPIOperationStatus.Pending.Login

        // checks if the controller is initialized
        if (initialized) {
            analytics?.logOperation("Operation: LOGIN\n$terminal")

            val requestConverter = converterFactory.newDtoToStringConverter<ServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = ServiceResponse::class.java
            )

            val xml = try {
                requestConverter.convert(
                    ServiceRequest(
                        applicationSender = terminal.saleConfig.applicationName,
                        popId = terminal.saleConfig.poiId,
                        requestId = requestIdFactory.generateRequestId(),
                        requestType = ServiceRequestType.LOGIN.value,
                        workstationId = terminal.saleConfig.saleId,
                        posData = PosData(
                            timestamp = terminalConfig.timeNow().toISOString()
                        ),
                    )
                )
            } catch (e: Exception) {
                // In case of an exception when converting the TDO to XML we
                // set the state to `Error.DataHandling`.
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 0 request object could not be converted to XML.",
                    error = e
                )
                return@withOPIContext
            }

            analytics?.logCommunication(
                protocol = PROTOCOL_C0,
                message = "SENT:\n$xml"
            )

            // setup C0 communication
            channel0.setOnError(::communicationErrorHandler)

            channel0.open()

            // here the app waits for the channel 0 socket to connect to the terminal.
            while (!channel0.isConnected) {
                delay(WAIT_DELAY)
            }

            channel0.sendMessage(xml) { responseXml ->
                analytics?.logCommunication(
                    protocol = PROTOCOL_C0,
                    message = "RECEIVED:\n$responseXml"
                )

                val response = try {
                    responseConverter.convert(responseXml)
                } catch (e: Exception) {
                    // In case of an exception when converting the XML to the DTO we
                    // set the state to `Error.DataHandling`.
                    _operationState.value = OPIOperationStatus.Error.DataHandling(
                        message = "Channel 0 response XML could not be parsed.",
                        error = e
                    )
                    return@sendMessage
                }

                _operationState.value = when (OverallResult.find(response.overallResult)) {
                    OverallResult.SUCCESS -> OPIOperationStatus.LoggedIn

                    else -> OPIOperationStatus.Result.Error(
                        date = terminalConfig.timeNow(),
                        customerReceipt = "",
                        merchantReceipt = "",
                        rawData = responseXml
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

    override suspend fun initiateCardPayment(amount: BigDecimal, currency: ISOAlphaCurrency) =
        withOPIContext {
            // If the state is not LoggedIn, then we should drop the new request
            if (_operationState.value !is OPIOperationStatus.LoggedIn) return@withOPIContext

            _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

            // checks if the controller is initialized
            if (initialized) {
                analytics?.logOperation(
                    "Operation: CARD_PAYMENT(" +
                            "amount: $amount, " +
                            "currency: $currency)" +
                            "\n$terminal"
                )

                // setup C1 communication, it has to be setup before C0,
                // because once C0 request is sent, C1 needs to handle the intermediate communication
                handleChannel1Communication()

                val payload = CardServiceRequest(
                    applicationSender = terminal.saleConfig.applicationName,
                    popId = terminal.saleConfig.poiId,
                    requestId = requestIdFactory.generateRequestId(),
                    requestType = ServiceRequestType.CARD_PAYMENT.value,
                    workstationId = terminal.saleConfig.saleId,
                    posData = PosData(terminalConfig.timeNow().toISOString()),
                    totalAmount = TotalAmount(
                        value = amount.setScale(2),
                        currency = currency.value
                    )
                )

                val requestConverter = converterFactory.newDtoToStringConverter<CardServiceRequest>()
                val responseConverter = converterFactory.newStringToDtoConverter(
                    clazz = CardServiceResponse::class.java
                )

                // setup C0 communication
                handleC0Communication(payload, requestConverter, responseConverter)
            } else {
                // in case the controller is not initialized set the state to `Error.NotInitialised`
                _operationState.value = OPIOperationStatus.Error.NotInitialised
            }
        }

    override suspend fun initiatePaymentReversal(stan: String) = withOPIContext {
        // If the state is not LoggedIn, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.LoggedIn) return@withOPIContext

        _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

        // checks if the controller is initialized
        if (initialized) {
            analytics?.logOperation(
                "Operation: CARD_PAYMENT_REVERSAL(" +
                        "stan: $stan)" +
                        "\n$terminal"
            )

            // setup C1 communication, it has to be setup before C0,
            // because once C0 request is sent, C1 needs to handle the intermediate communication
            handleChannel1Communication()

            val payload = CardServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = requestIdFactory.generateRequestId(),
                requestType = ServiceRequestType.PAYMENT_REVERSAL.value,
                workstationId = terminal.saleConfig.saleId,
                posData = PosData(terminalConfig.timeNow().toISOString()),
                originalTransaction = OriginalTransaction(stan)
            )

            val requestConverter = converterFactory.newDtoToStringConverter<CardServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = CardServiceResponse::class.java
            )

            // setup C0 communication
            handleC0Communication(payload, requestConverter, responseConverter)
        } else {
            // in case the controller is not initialized set the state to `Error.NotInitialised`
            _operationState.value = OPIOperationStatus.Error.NotInitialised
        }
    }

    override suspend fun initiatePartialRefund(amount: BigDecimal, currency: ISOAlphaCurrency) =
        withOPIContext {
            // If the state is not LoggedIn, then we should drop the new request
            if (_operationState.value !is OPIOperationStatus.LoggedIn) return@withOPIContext

            _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

            // checks if the controller is initialized
            if (initialized) {
                analytics?.logOperation(
                    "Operation: PARTIAL_REFUND(" +
                            "amount: $amount, " +
                            "currency: $currency)" +
                            "\n$terminal"
                )

                // setup C1 communication, it has to be setup before C0,
                // because once C0 request is sent, C1 needs to handle the intermediate communication
                handleChannel1Communication()

                val payload = CardServiceRequest(
                    applicationSender = terminal.saleConfig.applicationName,
                    popId = terminal.saleConfig.poiId,
                    requestId = requestIdFactory.generateRequestId(),
                    requestType = ServiceRequestType.PAYMENT_REFUND.value,
                    workstationId = terminal.saleConfig.saleId,
                    posData = PosData(terminalConfig.timeNow().toISOString()),
                    totalAmount = TotalAmount(
                        value = amount.setScale(2),
                        currency = currency.value
                    )
                )

                val requestConverter = converterFactory
                    .newDtoToStringConverter<CardServiceRequest>()
                val responseConverter = converterFactory.newStringToDtoConverter(
                    clazz = CardServiceResponse::class.java
                )

                // setup C0 communication
                handleC0Communication(payload, requestConverter, responseConverter)
            } else {
                // in case the controller is not initialized set the state to `Error.NotInitialised`
                _operationState.value = OPIOperationStatus.Error.NotInitialised
            }
        }

    override suspend fun initiateReconciliation() = withOPIContext {
        // If the state is not LoggedIn, then we should drop the new request
        if (_operationState.value !is OPIOperationStatus.LoggedIn) return@withOPIContext

        _operationState.value = OPIOperationStatus.Pending.Operation(terminalConfig.timeNow())

        // checks if the controller is initialized
        if (initialized) {
            analytics?.logOperation("Operation: RECONCILIATION\n$terminal")

            // setup C1 communication, it has to be setup before C0,
            // because once C0 request is sent, C1 needs to handle the intermediate communication
            handleChannel1Communication()

            val payload = ServiceRequest(
                applicationSender = terminal.saleConfig.applicationName,
                popId = terminal.saleConfig.poiId,
                requestId = requestIdFactory.generateRequestId(),
                requestType = ServiceRequestType.RECONCILIATION.value,
                workstationId = terminal.saleConfig.saleId
            )

            val requestConverter = converterFactory.newDtoToStringConverter<ServiceRequest>()
            val responseConverter = converterFactory.newStringToDtoConverter(
                clazz = ServiceResponse::class.java
            )

            // setup C0 communication
            handleC0Communication(payload, requestConverter, responseConverter)
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

        channel1.setOnError(::communicationErrorHandler)
        channel1.setOnMessage(getC1MessageHandler(requestConverter, responseConverter))

        channel1.open()
    }

    /**
     * This method returns the message listener for OPI channel 1.
     * the lambda converts the received XML message to the DTO,
     * then based on the message it sets the controller state and
     * answers according to the OPI protocol.
     */
    private fun getC1MessageHandler(
        requestConverter: StringToDtoConverter<DeviceRequest>,
        responseConverter: DtoToStringConverter<DeviceResponse>
    ): (String) -> Unit = { requestXml ->
        analytics?.logCommunication(
            protocol = PROTOCOL_C1,
            message = "RECEIVED:\n$requestXml"
        )

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
                    _operationState.value = handleC1OutputState(currentState, output)
                }
                DeviceRequestType.INPUT -> {
                    // TODO> handle input requests
                }
                DeviceRequestType.UNKNOWN -> Unit
            }

            // Sending response
            try {
                responseConverter.convert(
                    DeviceResponse(
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
                    )
                )
            } catch (e: Exception) {
                // In case of an exception when converting the DTO to XML we
                // set the state to `Error.DataHandling`.
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 1 response object could not be converted to XML.",
                    error = e
                )
                null
            }?.let {
                analytics?.logCommunication(
                    protocol = PROTOCOL_C1,
                    message = "SENT:\n$it"
                )
                channel1.sendMessage(it)
            }
        }
    }

    /**
     * This method sets up communication on OPI channel 0,
     * it opens the socket, sets up the error listener, converts to payload,
     * and sends the XML payload.
     */
    @Suppress("LongMethod")
    private suspend fun <U, V : OPIResponse> handleC0Communication(
        payload: U,
        requestConverter: DtoToStringConverter<U>,
        responseConverter: StringToDtoConverter<V>
    ) {
        if (_operationState.value is OPIOperationStatus.Error) {
            return
        }

        channel0.setOnError(::communicationErrorHandler)

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

        analytics?.logCommunication(
            protocol = PROTOCOL_C0,
            message = "SENT:\n$xml"
        )

        // here the app waits for the channel 0 socket to connect to the terminal.
        while (!channel0.isConnected) {
            delay(WAIT_DELAY)
        }

        channel0.sendMessage(xml) { responseXml ->
            analytics?.logCommunication(
                protocol = PROTOCOL_C0,
                message = "RECEIVED:\n$responseXml"
            )

            val response = try {
                responseConverter.convert(responseXml)
            } catch (e: Exception) {
                // In case of an exception when converting the XML to the DTO we
                // set the state to `Error.DataHandling`.
                _operationState.value = OPIOperationStatus.Error.DataHandling(
                    message = "Channel 0 response XML could not be parsed.",
                    error = e
                )
                return@sendMessage
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
                    data = response as? CardServiceResponse,
                    reconciliationData = response as? ServiceResponse
                )

                else -> OPIOperationStatus.Result.Error(
                    date = terminalConfig.timeNow(),
                    customerReceipt = customerReceipt,
                    merchantReceipt = merchantReceipt,
                    rawData = responseXml,
                    data = response as? CardServiceResponse,
                    reconciliationData = response as? ServiceResponse
                )
            }

            // as per protocol, both channels are closed after C0 response
            finishOperation()
        }
    }

    private fun communicationErrorHandler(error: Throwable, message: String) {
        if (_operationState.value is OPIOperationStatus.Result) {
            Timber.tag("OPI_CHANNEL_CONTROLLER")
                .d("Operation done, error ignored.\nError: $message\n$error")
            return
        }

        _operationState.value = OPIOperationStatus.Error.Communication(message, error)
    }

    /**
     * This method provides an updated pending state based on the C1 Output message.
     * To provide the new state it needs the [currentState] and the received
     * [output] data.
     */
    private fun handleC1OutputState(
        currentState: OPIOperationStatus.Pending.Operation,
        output: Output
    ): OPIOperationStatus = when (DeviceType.find(output.outDeviceTarget)) {
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

    companion object {
        private const val WAIT_DELAY = 100L
        private const val PROTOCOL_C0 = "OPI: Channel 0"
        private const val PROTOCOL_C1 = "OPI: Channel 1"
    }
}