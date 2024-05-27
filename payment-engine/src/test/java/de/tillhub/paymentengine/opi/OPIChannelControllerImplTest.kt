package de.tillhub.paymentengine.opi

import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.helper.TerminalConfig
import de.tillhub.paymentengine.helper.toInstant
import de.tillhub.paymentengine.opi.communication.OPIChannel0
import de.tillhub.paymentengine.opi.communication.OPIChannel1
import de.tillhub.paymentengine.opi.communication.OPIChannelFactory
import de.tillhub.paymentengine.opi.data.Authorisation
import de.tillhub.paymentengine.opi.data.CardServiceRequest
import de.tillhub.paymentengine.opi.data.CardServiceResponse
import de.tillhub.paymentengine.opi.data.ConverterFactory
import de.tillhub.paymentengine.opi.data.ConvertersTest
import de.tillhub.paymentengine.opi.data.DeviceRequest
import de.tillhub.paymentengine.opi.data.DeviceResponse
import de.tillhub.paymentengine.opi.data.DtoToStringConverter
import de.tillhub.paymentengine.opi.data.OPIOperationStatus
import de.tillhub.paymentengine.opi.data.OverallResult
import de.tillhub.paymentengine.opi.data.PrivateData
import de.tillhub.paymentengine.opi.data.Reconciliation
import de.tillhub.paymentengine.opi.data.RequestIdFactory
import de.tillhub.paymentengine.opi.data.ServiceRequest
import de.tillhub.paymentengine.opi.data.ServiceRequestType
import de.tillhub.paymentengine.opi.data.ServiceResponse
import de.tillhub.paymentengine.opi.data.StringToDtoConverter
import de.tillhub.paymentengine.opi.data.TotalAmount
import de.tillhub.paymentengine.opi.data.ValueElement
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class OPIChannelControllerImplTest : DescribeSpec({
    lateinit var converterFactory: ConverterFactory
    lateinit var channelFactory: OPIChannelFactory
    lateinit var terminalConfig: TerminalConfig
    lateinit var requestIdFactory: RequestIdFactory

    lateinit var target: OPIChannelControllerImpl

    lateinit var serviceRequestConverter: DtoToStringConverter<ServiceRequest>
    lateinit var serviceResponseConverter: StringToDtoConverter<ServiceResponse>
    lateinit var cardServiceRequestConverter: DtoToStringConverter<CardServiceRequest>
    lateinit var cardServiceResponseConverter: StringToDtoConverter<CardServiceResponse>
    lateinit var deviceResponseConverter: DtoToStringConverter<DeviceResponse>
    lateinit var deviceRequestConverter: StringToDtoConverter<DeviceRequest>

    lateinit var opiChannel0: OPIChannel0
    lateinit var opiChannel1: OPIChannel1

    var c0OnMessage: ((String) -> Unit)? = null
    var c1OnMessage: ((String) -> Unit)? = null

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

        opiChannel0 = mockk {
            every { setOnError(any()) } just Runs
            every { open() } just Runs
            every { close() } just Runs
            every { isConnected } returns true
            every { sendMessage(any(), any()) } answers {
                val callback = secondArg<(String) -> Unit>()

                c0OnMessage = callback
            }
        }
        opiChannel1 = mockk {
            every { setOnError(any()) } just Runs
            every { open() } just Runs
            every { close() } just Runs
            every { isConnected } returns true
            every { setOnMessage(any()) } answers {
                val callback = firstArg<(String) -> Unit>()

                c1OnMessage = callback
            }
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
            requestIdFactory
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
                    converterFactory.newDtoToStringConverter<ServiceRequest>()
                    converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                    requestIdFactory.generateRequestId()
                    terminalConfig.timeNow()
                    serviceRequestConverter.convert(any())
                    opiChannel0.setOnError(any())
                    opiChannel0.open()
                    opiChannel0.sendMessage(ConvertersTest.SERVICE_REQUEST_XML, any())
                }

                c0OnMessage?.invoke(ConvertersTest.SERVICE_RESPONSE_XML)

                target.operationState.value shouldBe OPIOperationStatus.LoggedIn

                verify(Ordering.ORDERED) {
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
                    converterFactory.newDtoToStringConverter<ServiceRequest>()
                    converterFactory.newStringToDtoConverter(ServiceResponse::class.java)
                    requestIdFactory.generateRequestId()
                    terminalConfig.timeNow()
                    serviceRequestConverter.convert(any())
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
                    serviceResponseConverter.convert(ConvertersTest.SERVICE_RESPONSE_XML)
                    opiChannel0.close()
                    opiChannel1.close()
                }
            }
        }
    }

    describe("initiateCardPayment") {
        beforeTest {
            every { converterFactory.newDtoToStringConverter<ServiceRequest>() } returns serviceRequestConverter
            every { converterFactory.newStringToDtoConverter(ServiceResponse::class.java) } returns serviceResponseConverter

            target.init(TERMINAL)
            target.login()
        }


    }

}) {
    companion object {
        val NOW = "2024-02-09T09:36:36".toInstant()

        val TERMINAL = Terminal.OPI(
            name = "opi",
            ipAddress = "192.168.1.22",
            port = 20002,
            port2 = 20007
        )

        val ERROR_SERVICE_RESPONSE = ServiceResponse(
            requestId = "20792",
            requestType = ServiceRequestType.LOGIN.value,
            workstationID = "1",
            overallResult = OverallResult.FAILURE.value,
        )
    }
}