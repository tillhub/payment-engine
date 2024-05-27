package de.tillhub.paymentengine.opi.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ConvertersTest : FunSpec({

    test("CardServiceRequest") {
        val target = DtoToStringConverter<CardServiceRequest>()

        val result = target.convert(CARD_SERVICE_REQUEST)

        result shouldBe CARD_SERVICE_REQUEST_XML
    }

    test("CardServiceResponse") {
        val target = StringToDtoConverter(CardServiceResponse::class.java)

        val result = target.convert(CARD_SERVICE_RESPONSE_XML)

        result shouldBe CARD_SERVICE_RESPONSE
    }

    test("ServiceRequest") {
        val target = DtoToStringConverter<ServiceRequest>()

        val result = target.convert(SERVICE_REQUEST)

        result shouldBe SERVICE_REQUEST_XML
    }

    test("ServiceResponse") {
        val target = StringToDtoConverter(ServiceResponse::class.java)

        val result = target.convert(SERVICE_RESPONSE_XML)

        result shouldBe SERVICE_RESPONSE
    }

    test("DeviceRequest") {
        val target = StringToDtoConverter(DeviceRequest::class.java)

        val result = target.convert(DEVICE_REQUEST_XML)

        result shouldBe DEVICE_REQUEST
    }

    test("DeviceResponse") {
        val target = DtoToStringConverter<DeviceResponse>()

        val result = target.convert(DEVICE_RESPONSE)

        result shouldBe DEVICE_RESPONSE_XML
    }
}) {
    companion object {
        val CARD_SERVICE_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<CardServiceRequest ApplicationSender=\"TillhubIOS\" POPID=\"001\" " +
                "RequestID=\"20544\" RequestType=\"CardPayment\" " +
                "WorkstationID=\"1\" xmlns=\"http://www.nrf-arts.org/IXRetail/namespace\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "   <POSdata>\n" +
                "      <POSTimeStamp>2024-02-09T09:36:36</POSTimeStamp>\n" +
                "   </POSdata>\n" +
                "   <TotalAmount Currency=\"EUR\">39.5</TotalAmount>\n" +
                "</CardServiceRequest>"
        val CARD_SERVICE_REQUEST = CardServiceRequest(
            requestType = ServiceRequestType.CARD_PAYMENT.value,
            applicationSender = "TillhubIOS",
            popId = "001",
            requestId = "20544",
            workstationId = "1",
            posData = PosData(
                timestamp = "2024-02-09T09:36:36"
            ),
            totalAmount = TotalAmount(
                currency = "EUR",
                value = 39.50.toBigDecimal()
            )
        )

        val CARD_SERVICE_RESPONSE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <CardServiceResponse
            	xmlns="http://www.nrf-arts.org/IXRetail/namespace"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xsi:schemaLocation="http://www.nrf-arts.org/IXRetail/namespace file:///C:/Windows/O.P.I.Schema/CardResponse.xsd" RequestType="CardPayment" WorkstationID="1" RequestID="20544" OverallResult="Success">
            	<Terminal TerminalID="68276185" STAN="021601"/>
            	<Tender>
            		<TotalAmount Currency="EUR">39.50</TotalAmount>
            		<Authorisation AuthorisationType="Online" CardCircuit="girocard" CardPAN="6805330010086784747" Merchant="VU-123456789017" ReceiptNumber="8930" ActionCode="0" TimeStamp="2024-02-09T09:36:28-00:00" AcquirerID="undefined"/>
            	</Tender>
            	<PrivateData>
            		<CardTechnologyType>NFC</CardTechnologyType>
            		<CardVerificationMethod>NoCVM</CardVerificationMethod>
            		<ErrorCode>1002700</ErrorCode>
            		<ErrorText>Zahlung erfolgt</ErrorText>
            	</PrivateData>
            	<CardValue>
            		<ExpiryDate>1227</ExpiryDate>
            	</CardValue>
            </CardServiceResponse>
        """.trimIndent()
        val CARD_SERVICE_RESPONSE = CardServiceResponse(
            requestType = ServiceRequestType.CARD_PAYMENT.value,
            requestId = "20544",
            workstationId = "1",
            overallResult = OverallResult.SUCCESS.value,
            terminal = Terminal(
                terminalId = "68276185",
                stan = "021601"
            ),
            tender = Tender(
                totalAmount = TotalAmount(
                    currency = "EUR",
                    value = 39.50.toBigDecimal().setScale(2)
                ),
                authorisation = Authorisation(
                    authorisationType = "Online",
                    cardCircuit = "girocard",
                    cardPAN = "6805330010086784747",
                    merchant = "VU-123456789017",
                    receiptNumber = "8930",
                    actionCode = "0",
                    timeStamp = "2024-02-09T09:36:28-00:00",
                    acquirerID = "undefined"
                )
            ),
            privateData = PrivateData(
                cardTechnologyType = ValueElement("NFC"),
                cardVerificationMethod = ValueElement("NoCVM"),
                errorCode = ValueElement("1002700"),
            ),
            cardValue = CardValue(
                expiryDate = ValueElement("1227")
            )
        )

        val SERVICE_REQUEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ServiceRequest " +
                "ApplicationSender=\"TillhubIOS\" POPID=\"001\" RequestID=\"20793\" " +
                "RequestType=\"Login\" WorkstationID=\"1\" " +
                "xmlns=\"http://www.nrf-arts.org/IXRetail/namespace\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "   <POSdata>\n" +
                "      <POSTimeStamp>2024-02-21T07:59:01</POSTimeStamp>\n" +
                "   </POSdata>\n" +
                "</ServiceRequest>"
        val SERVICE_REQUEST = ServiceRequest(
            applicationSender = "TillhubIOS",
            popId = "001",
            requestId = "20793",
            requestType = ServiceRequestType.LOGIN.value,
            workstationId = "1",
            posData = PosData(
                timestamp = "2024-02-21T07:59:01"
            )
        )

        val SERVICE_RESPONSE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ServiceResponse
                xmlns="http://www.nrf-arts.org/IXRetail/namespace"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xsi:schemaLocation="http://www.nrf-arts.org/IXRetail/namespace file:///C:/Windows/O.P.I.Schema/ServiceResponse.xsd" 
                RequestType="ReconciliationWithClosure" WorkstationID="1" RequestID="20792" OverallResult="Success">
                <Terminal TerminalID="68276185" STAN="021888"/>
                <Authorisation TimeStamp="2024-02-20T18:58:39-00:00"/>
                <Reconciliation>
                    <TotalAmount NumberPayments="8" PaymentType="Debit" Currency="EUR" CardCircuit="girocard">338.00</TotalAmount>
                    <TotalAmount NumberPayments="3" PaymentType="Debit" Currency="EUR" CardCircuit="VISA">87.00</TotalAmount>
                </Reconciliation>
                <PrivateData>
                    <ErrorCode>1002700</ErrorCode>
                </PrivateData>
            </ServiceResponse>
        """.trimIndent()
        val SERVICE_RESPONSE = ServiceResponse(
            requestId = "20792",
            requestType = ServiceRequestType.RECONCILIATION.value,
            workstationID = "1",
            overallResult = OverallResult.SUCCESS.value,
            terminal = Terminal(
                terminalId = "68276185",
                stan = "021888"
            ),
            authorisation = Authorisation(
                timeStamp = "2024-02-20T18:58:39-00:00"
            ),
            reconciliation = Reconciliation(
                totalAmounts = listOf(
                    TotalAmount(
                        numberPayments = 8,
                        paymentType = "Debit",
                        cardCircuit = "girocard",
                        value = 338.00.toBigDecimal().setScale(2),
                        currency = "EUR"
                    ),
                    TotalAmount(
                        numberPayments = 3,
                        paymentType = "Debit",
                        cardCircuit = "VISA",
                        value = 87.00.toBigDecimal().setScale(2),
                        currency = "EUR"
                    )
                )
            ),
            privateData = PrivateData(
                errorCode = ValueElement("1002700")
            )
        )

        val DEVICE_REQUEST_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <DeviceRequest RequestType="Output" RequestID="20792" WorkstationID="1" SequenceID="2" TerminalID="68276185" ApplicationSender="Thales-OPI"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="file:///C:/Windows/O.P.I.Schema/DeviceRequest.xsd">
            	<Output OutDeviceTarget="CashierDisplay">
            		<TextLine Column="0" Row="0">Vorgang wird</TextLine>
            		<TextLine Column="0" Row="1">bearbeitet</TextLine>
            	</Output>
            </DeviceRequest>
        """.trimIndent()
        val DEVICE_REQUEST = DeviceRequest(
            requestType = DeviceRequestType.OUTPUT.value,
            requestId = "20792",
            workstationId = "1",
            applicationSender = "Thales-OPI",
            output = listOf(
                Output(
                    outDeviceTarget = DeviceType.CASHIER_DISPLAY.value,
                    textLines = listOf(
                        TextLine(column = 0, row = 0, value = "Vorgang wird"),
                        TextLine(column = 0, row = 1, value = "bearbeitet")
                    )
                )
            )
        )

        val DEVICE_RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<DeviceResponse ApplicationSender=\"WINPOS\" POPID=\"001\" RequestID=\"20792\" " +
                "RequestType=\"Output\" WorkstationID=\"1\" OverallResult=\"Success\" " +
                "xmlns=\"http://www.nrf-arts.org/IXRetail/namespace\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "   <Output OutDeviceTarget=\"CustomerDisplay\" OutResult=\"Success\"/>\n" +
                "</DeviceResponse>"
        val DEVICE_RESPONSE = DeviceResponse(
            applicationSender = "WINPOS",
            overallResult = OverallResult.SUCCESS.value,
            requestId = "20792",
            workstationID = "1",
            popId = "001",
            requestType = DeviceRequestType.OUTPUT.value,
            output = listOf(
                Output(
                    outDeviceTarget = DeviceType.CUSTOMER_DISPLAY.value,
                    outResult = OverallResult.SUCCESS.value
                )
            )
        )
    }
}