package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.toInstant
import de.tillhub.paymentengine.opi.communication.OPIChannel0
import de.tillhub.paymentengine.opi.communication.OPIChannel1
import de.tillhub.paymentengine.opi.communication.OPIChannelFactory
import de.tillhub.paymentengine.opi.data.CardServiceRequest
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.ConvertersTest
import de.tillhub.paymentengine.opi.data.DeviceRequest
import de.tillhub.paymentengine.opi.data.DeviceRequestType
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.DeviceType
import de.tillhub.paymentengine.opi.data.DtoToStringConverter
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.data.OPITerminal
import de.tillhub.paymentengine.opi.data.OriginalTransaction
import de.tillhub.paymentengine.opi.data.Output
import de.tillhub.paymentengine.opi.data.OverallResult
import de.tillhub.paymentengine.opi.data.PosData
import de.tillhub.paymentengine.opi.data.RequestIdFactory
import de.tillhub.paymentengine.opi.data.ServiceRequest
import de.tillhub.paymentengine.opi.data.ServiceRequestType
import de.tillhub.paymentengine.opi.data.ServiceResponse
import de.tillhub.paymentengine.opi.data.StringToDtoConverter
import de.tillhub.paymentengine.opi.data.TextLine
import de.tillhub.paymentengine.opi.data.TotalAmount
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("LargeClass")
@ExperimentalCoroutinesApi
internal class OPIChannelControllerImplTest : DescribeSpec({
    lateinit var converterFactory: ConverterFactory
    lateinit var channelFactory: OPIChannelFactory
    lateinit var terminalConfig: TerminalConfig
    lateinit var requestIdFactory: RequestIdFactory
    lateinit var analytics: PaymentAnalytics

    lateinit var target: OPIChannelControllerImpl

    lateinit var serviceRequestConverter: DtoToStringConverter<ServiceRequest>
    lateinit var serviceResponseConverter: StringToDtoConverter<ServiceResponse>
    lateinit var cardServiceRequestConverter: DtoToStringConverter<CardServiceRequest>
    lateinit var cardServiceResponseConverter: StringToDtoConverter<CardServiceResponse>
    lateinit var deviceResponseConverter: DtoToStringConverter<DeviceResponse>
    lateinit var deviceRequestConverter: StringToDtoConverter<DeviceRequest>

    lateinit var opiChannel0: OPIChannel0
    lateinit var opiChannel1: OPIChannel1

    var c0OnMessage: (suspend (String) -> Unit)? = null
    var c1OnMessage: ((String) -> Unit)? = null

    var c0OnError: ((Throwable, String) -> Unit)? = null
    var c1OnError: ((Throwable, String) -> Unit)? = null

    beforeAny {
        serviceRequestConverter = mockk {
            every { convert(any()) } returns ConvertersTest.SERVICE_REQUEST_XML
        }
        serviceResponseConverter = mockk {
            every { convert(any()) } returns ConvertersTest.SERVICE_RESPONSE
        }
        cardServiceRequestConverter = mockk {
            every { convert(any()) } returns ConvertersTest.CARD_SERVICE_REQUEST_XML
        }
        cardServiceResponseConverter = mockk {
            every { convert(any()) } returns ConvertersTest.CARD_SERVICE_RESPONSE
        }
        deviceResponseConverter = mockk {
            every { convert(any()) } returns ConvertersTest.DEVICE_RESPONSE_XML
        }
        deviceRequestConverter = mockk {
            every { convert(any()) } returns ConvertersTest.DEVICE_REQUEST
        }
        analytics = mockk {
            every { logOperation(any()) } just Runs
            every { logCommunication(any(), any()) } just Runs
        }

        opiChannel0 = mockk {
            every { setOnError(any()) } answers {
                c0OnError = firstArg<(Throwable, String) -> Unit>()
            }
            every { open() } just Runs
            every { close() } just Runs
            every { isConnected } returns true
            every { sendMessage(any(), any()) } answers {
                val callback = secondArg<suspend (String) -> Unit>()

                c0OnMessage = callback
            }
        }
        opiChannel1 = mockk {
            every { setOnError(any()) } answers {
                c1OnError = firstArg<(Throwable, String) -> Unit>()
            }
            every { open() } just Runs
            every { close() } just Runs
            every { isConnected } returns true
            every { setOnMessage(any()) } answers {
                val callback = firstArg<(String) -> Unit>()

                c1OnMessage = callback
            }
            every { sendMessage(any()) } just Runs
        }

        converterFactory = mockk()
        channelFactory = mockk {
            every { newOPIChannel0(any(), any()) } returns opiChannel0
            every { newOPIChannel1(any()) } returns opiChannel1
        }
        terminalConfig = mockk {
            every { timeNow() } returns NOW
        }
        requestIdFactory = mockk {
            every { generateRequestId() } returns "12345678"
        }

        target = OPIChannelControllerImpl(
            converterFactory,
            channelFactory,
            terminalConfig,
            requestIdFactory,
            analytics
        )
    }

    it("initialize & close") {
        target.init(TERMINAL)

        target.close()

        verify(Ordering.ORDERED) {
            channelFactory.newOPIChannel0(TERMINAL.ipAddress, TERMINAL.port)
            channelFactory.newOPIChannel1(TERMINAL.port2)

            opiChannel0.close()
            opiChannel1.close()
        }
    }

    describe("login") {
        it("NOT initialized") {
            target.login()

            target.operationState.value shouldBe OPIOperationStatus.Error.NotInitialised
        }

        describe("initialized") {
            beforeTest {
                every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
                every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

                target.init(TERMINAL)
            }

            it("SUCCESS") {
                target.login()

                target.operationState.value shouldBe OPIOperationStatus.Pending.Login

                verify(Ordering.ORDERED) {
                    analytics.logOperation("Operation: LOGIN\n$TERMINAL_STRING")
                    converterFactory.newDtoToStringConverter<ServiceRequest>()
                    converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                    requestIdFactory.generateRequestId()
                    terminalConfig.timeNow()
                    serviceRequestConverter.convert(
                        ServiceRequest(
                            applicationSender = TERMINAL.saleConfig.applicationName,
                            popId = TERMINAL.saleConfig.poiId,
                            requestId = "12345678",
                            requestType = "Login",
                            workstationId = TERMINAL.saleConfig.saleId,
                            posData = PosData(
                                timestamp = "2024-02-09T09:36:36Z"
                            ),
                        )
                    )
                    analytics.logCommunication(
                        protocol = "OPI: Channel 0",
                        message = "SENT:\n${ConvertersTest.SERVICE_REQUEST_XML}"
                    )
                    opiChannel0.setOnError(any())
                    opiChannel0.open()
                    opiChannel0.sendMessage(ConvertersTest.SERVICE_REQUEST_XML, any())
                }

                c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

                target.operationState.value shouldBe OPIOperationStatus.LoggedIn

                verify(Ordering.ORDERED) {
                    analytics.logCommunication(
                        protocol = "OPI: Channel 0",
                        message = "RECEIVED:\n${ConvertersTest.SERVICE_RESPONSE_XML}"
                    )
                    serviceResponseConverter.convert(ConvertersTest.SERVICE_RESPONSE_XML)
                    opiChannel0.close()
                    opiChannel1.close()
                }
            }

            it("ERROR") {
                every { serviceResponseConverter.convert(any()) } returns ERROR_SERVICE_RESPONSE
                target.login()

                target.operationState.value shouldBe OPIOperationStatus.Pending.Login

                verify(Ordering.ORDERED) {
                    analytics.logOperation("Operation: LOGIN\n$TERMINAL_STRING")
                    converterFactory.newDtoToStringConverter<ServiceRequest>()
                    converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                    requestIdFactory.generateRequestId()
                    terminalConfig.timeNow()
                    serviceRequestConverter.convert(
                        ServiceRequest(
                            applicationSender = TERMINAL.saleConfig.applicationName,
                            popId = TERMINAL.saleConfig.poiId,
                            requestId = "12345678",
                            requestType = "Login",
                            workstationId = TERMINAL.saleConfig.saleId,
                            posData = PosData(
                                timestamp = "2024-02-09T09:36:36Z"
                            ),
                        )
                    )
                    analytics.logCommunication(
                        protocol = "OPI: Channel 0",
                        message = "SENT:\n${ConvertersTest.SERVICE_REQUEST_XML}"
                    )
                    opiChannel0.setOnError(any())
                    opiChannel0.open()
                    opiChannel0.sendMessage(ConvertersTest.SERVICE_REQUEST_XML, any())
                }

                c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

                target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                    date = NOW,
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = ConvertersTest.SERVICE_RESPONSE_XML
                )

                verify(Ordering.ORDERED) {
                    analytics.logCommunication(
                        protocol = "OPI: Channel 0",
                        message = "RECEIVED:\n${ConvertersTest.SERVICE_RESPONSE_XML}"
                    )
                    serviceResponseConverter.convert(ConvertersTest.SERVICE_RESPONSE_XML)
                    opiChannel0.close()
                    opiChannel1.close()
                }
            }

            it("initiateLogin") {
                target.login()

                c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

                target.initiateLogin()

                target.operationState.value shouldBe OPIOperationStatus.Result.Success(
                    date = NOW,
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = "",
                    data = null,
                    serviceData = ConvertersTest.SERVICE_RESPONSE
                )
            }
        }
    }

    describe("initiateCardPayment") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)
        }

        it("SUCCESS") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateCardPayment(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "CardPayment",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        totalAmount = TotalAmount(
                            value = 6.0.toBigDecimal().setScale(2),
                            currency = "EUR"
                        )
                    )
                )
                analytics.logCommunication(
                    protocol = "OPI: Channel 0",
                    message = "SENT:\n${ConvertersTest.CARD_SERVICE_REQUEST_XML}"
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { deviceRequestConverter.convert(any()) } returns ConvertersTest.DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                )
            )

            verify {
                analytics.logCommunication(
                    protocol = "OPI: Channel 1",
                    message = "RECEIVED:\n${ConvertersTest.DEVICE_REQUEST_XML}"
                )
                analytics.logCommunication(
                    protocol = "OPI: Channel 1",
                    message = "SENT:\n${ConvertersTest.DEVICE_RESPONSE_XML}"
                )
            }

            every { deviceRequestConverter.convert(any()) } returns CUSTOMER_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n"
            )

            every { deviceRequestConverter.convert(any()) } returns MERCHANT_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
            )

            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Success(
                date = NOW,
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ConvertersTest.CARD_SERVICE_RESPONSE,
                serviceData = null
            )

            verify {
                analytics.logCommunication(
                    protocol = "OPI: Channel 0",
                    message = "RECEIVED:\n${ConvertersTest.CARD_SERVICE_RESPONSE_XML}"
                )
            }
        }

        it("ERROR") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateCardPayment(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "CardPayment",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        totalAmount = TotalAmount(
                            value = 6.0.toBigDecimal().setScale(2),
                            currency = "EUR"
                        )
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { cardServiceResponseConverter.convert(any()) } returns ERROR_CARD_SERVICE_RESPONSE
            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                date = NOW,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ERROR_CARD_SERVICE_RESPONSE,
                serviceData = null
            )
        }
    }

    describe("initiatePaymentReversal") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)
        }

        it("SUCCESS") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiatePaymentReversal("223")

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "PaymentReversal",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        originalTransaction = OriginalTransaction("223")
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { deviceRequestConverter.convert(any()) } returns ConvertersTest.DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                )
            )

            every { deviceRequestConverter.convert(any()) } returns CUSTOMER_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n"
            )

            every { deviceRequestConverter.convert(any()) } returns MERCHANT_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
            )

            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Success(
                date = NOW,
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ConvertersTest.CARD_SERVICE_RESPONSE,
                serviceData = null
            )
        }

        it("ERROR") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiatePaymentReversal("223")

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "PaymentReversal",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        originalTransaction = OriginalTransaction("223")
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { cardServiceResponseConverter.convert(any()) } returns ERROR_CARD_SERVICE_RESPONSE
            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                date = NOW,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ERROR_CARD_SERVICE_RESPONSE,
                serviceData = null
            )
        }
    }

    describe("initiatePartialRefund") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)
        }

        it("SUCCESS") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiatePartialRefund(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "PaymentRefund",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        totalAmount = TotalAmount(
                            value = 6.0.toBigDecimal().setScale(2),
                            currency = "EUR"
                        )
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { deviceRequestConverter.convert(any()) } returns ConvertersTest.DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                )
            )

            every { deviceRequestConverter.convert(any()) } returns CUSTOMER_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n"
            )

            every { deviceRequestConverter.convert(any()) } returns MERCHANT_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
            )

            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Success(
                date = NOW,
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ConvertersTest.CARD_SERVICE_RESPONSE,
                serviceData = null
            )
        }

        it("ERROR") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiatePartialRefund(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "PaymentRefund",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                        totalAmount = TotalAmount(
                            value = 6.0.toBigDecimal().setScale(2),
                            currency = "EUR"
                        )
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { cardServiceResponseConverter.convert(any()) } returns ERROR_CARD_SERVICE_RESPONSE
            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                date = NOW,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = ERROR_CARD_SERVICE_RESPONSE,
                serviceData = null
            )
        }
    }

    describe("initiateReconciliation") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)
        }

        it("SUCCESS") {
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateReconciliation()

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<ServiceRequest>()
                converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                serviceRequestConverter.convert(
                    ServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "ReconciliationWithClosure",
                        workstationId = TERMINAL.saleConfig.saleId,
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.SERVICE_REQUEST_XML, any())
            }

            every { deviceRequestConverter.convert(any()) } returns ConvertersTest.DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                )
            )

            every { deviceRequestConverter.convert(any()) } returns CUSTOMER_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n"
            )

            every { deviceRequestConverter.convert(any()) } returns MERCHANT_RECEIPT_DEVICE_REQUEST
            c1OnMessage?.invoke(ConvertersTest.DEVICE_REQUEST_XML)

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(
                date = NOW,
                messageLines = listOf(
                    "Vorgang wird",
                    "bearbeitet"
                ),
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
            )

            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Success(
                date = NOW,
                customerReceipt = "Customer receipt\nline 1\nline 2\n",
                merchantReceipt = "Merchant receipt\nline 1\nline 2\n",
                rawData = ConvertersTest.SERVICE_RESPONSE_XML,
                data = null,
                serviceData = ConvertersTest.SERVICE_RESPONSE
            )
        }

        it("ERROR") {
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateReconciliation()

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<ServiceRequest>()
                converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                serviceRequestConverter.convert(
                    ServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "ReconciliationWithClosure",
                        workstationId = TERMINAL.saleConfig.saleId,
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.SERVICE_REQUEST_XML, any())
            }

            every { serviceResponseConverter.convert(any()) } returns ERROR_SERVICE_RESPONSE.copy(
                requestType = "ReconciliationWithClosure"
            )
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                date = NOW,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ConvertersTest.SERVICE_RESPONSE_XML,
                data = null,
                serviceData = ERROR_SERVICE_RESPONSE.copy(
                    requestType = "ReconciliationWithClosure"
                )
            )
        }
    }

    describe("abortRequest") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateCardPayment(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))
        }

        it("SUCCESS") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.abortRequest()

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "AbortRequest",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        ),
                    )
                )
                analytics.logCommunication(
                    protocol = "OPI: Channel 0",
                    message = "SENT:\n${ConvertersTest.CARD_SERVICE_REQUEST_XML}"
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            verify {
                analytics.logCommunication(
                    protocol = "OPI: Channel 0",
                    message = "RECEIVED:\n${ConvertersTest.CARD_SERVICE_RESPONSE_XML}"
                )
            }
        }

        it("ERROR") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.abortRequest()

            verify(Ordering.ORDERED) {
                converterFactory.newStringToDtoConverter(DeviceRequest::class.java)
                converterFactory.newDtoToStringConverter<DeviceResponse>()
                opiChannel1.setOnError(any())
                opiChannel1.setOnMessage(any())
                opiChannel1.open()
                converterFactory.newDtoToStringConverter<CardServiceRequest>()
                converterFactory.newStringToDtoConverter(CardServiceResponse::class.java)
                opiChannel0.setOnError(any())
                opiChannel0.open()
                cardServiceRequestConverter.convert(
                    CardServiceRequest(
                        applicationSender = TERMINAL.saleConfig.applicationName,
                        popId = TERMINAL.saleConfig.poiId,
                        requestId = "12345678",
                        requestType = "AbortRequest",
                        workstationId = TERMINAL.saleConfig.saleId,
                        posData = PosData(
                            timestamp = "2024-02-09T09:36:36Z"
                        )
                    )
                )
                opiChannel0.isConnected
                opiChannel0.sendMessage(ConvertersTest.CARD_SERVICE_REQUEST_XML, any())
            }

            every { cardServiceResponseConverter.convert(any()) } returns ERROR_CARD_SERVICE_RESPONSE
            c0OnMessage?.invoke(ConvertersTest.CARD_SERVICE_RESPONSE_XML)

            target.operationState.value shouldBe OPIOperationStatus.Result.Error(
                date = NOW,
                customerReceipt = "",
                merchantReceipt = "",
                rawData = ConvertersTest.CARD_SERVICE_RESPONSE_XML,
                data = null,
                serviceData = null
            )
        }
    }

    describe("channel errors") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
            c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)
        }

        it("channel 1 errors") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateCardPayment(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            val err = Throwable()
            c1OnError?.invoke(err, "C1 error")

            target.operationState.value shouldBe OPIOperationStatus.Error.Communication("C1 error", err)
        }

        it("channel 0 errors") {
            every { converterFactory.newStringToDtoConverter(CardServiceResponse::class.java) } returns cardServiceResponseConverter
            every { converterFactory.newDtoToStringConverter<DeviceResponse>() } answers {
                every { converterFactory.newDtoToStringConverter<CardServiceRequest>() } returns cardServiceRequestConverter

                deviceResponseConverter
            }
            every { converterFactory.newStringToDtoConverter(DeviceRequest::class.java) } returns deviceRequestConverter

            target.initiateCardPayment(6.0.toBigDecimal(), ISOAlphaCurrency("EUR"))

            target.operationState.value shouldBe OPIOperationStatus.Pending.Operation(NOW)

            val err = Throwable()
            c0OnError?.invoke(err, "C0 error")

            target.operationState.value shouldBe OPIOperationStatus.Error.Communication("C0 error", err)
        }
    }
}) {
    companion object {
        val NOW = "2024-02-09T09:36:36Z".toInstant()

        val TERMINAL = OPITerminal(
            id = "opi",
            ipAddress = "192.168.1.22",
            port = 20002,
            port2 = 20007
        )

        val TERMINAL_STRING = TERMINAL.toString()

        val ERROR_SERVICE_RESPONSE = ServiceResponse(
            requestId = "20792",
            requestType = ServiceRequestType.LOGIN.value,
            workstationID = "1",
            overallResult = OverallResult.FAILURE.value,
        )

        val CUSTOMER_RECEIPT_DEVICE_REQUEST = DeviceRequest(
            requestType = DeviceRequestType.OUTPUT.value,
            requestId = "20792",
            workstationId = "1",
            applicationSender = "Thales-OPI",
            output = listOf(
                Output(
                    outDeviceTarget = DeviceType.PRINTER_RECEIPT.value,
                    textLines = listOf(
                        TextLine(value = "Customer receipt"),
                        TextLine(value = "line 1"),
                        TextLine(value = "line 2")
                    )
                )
            )
        )

        val MERCHANT_RECEIPT_DEVICE_REQUEST = DeviceRequest(
            requestType = DeviceRequestType.OUTPUT.value,
            requestId = "20792",
            workstationId = "1",
            applicationSender = "Thales-OPI",
            output = listOf(
                Output(
                    outDeviceTarget = DeviceType.PRINTER.value,
                    textLines = listOf(
                        TextLine(value = "Merchant receipt"),
                        TextLine(value = "line 1"),
                        TextLine(value = "line 2")
                    )
                )
            )
        )

        val ERROR_CARD_SERVICE_RESPONSE = CardServiceResponse(
            requestType = ServiceRequestType.CARD_PAYMENT.value,
            requestId = "20544",
            workstationId = "1",
            overallResult = OverallResult.FAILURE.value,
        )
    }
}