package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "CardServiceResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
data class CardServiceResponse(
    @field:Attribute(name = "ApplicationSender")
    @param:Attribute(name = "ApplicationSender")
    var applicationSender: String,
    @field:Attribute(name = "POPID")
    @param:Attribute(name = "POPID")
    var popId: String,
    @field:Attribute(name = "RequestID")
    @param:Attribute(name = "RequestID")
    var requestId: String,
    @field:Attribute(name = "RequestType")
    @param:Attribute(name = "RequestType")
    var requestType: String,
    @field:Attribute(name = "WorkstationID")
    @param:Attribute(name = "WorkstationID")
    var workstationID: String,
    @field:Attribute(name = "OverallResult")
    @param:Attribute(name = "OverallResult")
    var overallResult: String,

    @field:Element(name = "Terminal", required = false)
    @param:Element(name = "Terminal", required = false)
    var terminal: Terminal? = null,

    @field:Element(name = "Tender", required = false)
    @param:Element(name = "Tender", required = false)
    var tender: Tender? = null,

    @field:Element(name = "CardDetails", required = false)
    @param:Element(name = "CardDetails", required = false)
    var cardDetails: CardDetails? = null,

    @field:Element(name = "CardValue", required = false)
    @param:Element(name = "CardValue", required = false)
    var cardValue: CardValue? = null,

    @field:Element(name = "PrivateData", required = false)
    @param:Element(name = "PrivateData", required = false)
    var privateData: PrivateData? = null,
)

data class Terminal(
    @field:Attribute(name = "STAN")
    @param:Attribute(name = "STAN")
    var stan: String,
    @field:Attribute(name = "TerminalID")
    @param:Attribute(name = "TerminalID")
    var terminalId: String,
)

data class Tender(
    @field:Element(name = "Authorisation")
    @param:Element(name = "Authorisation")
    var authorisation: Authorisation,

    @field:Element(name = "TotalAmount")
    @param:Element(name = "TotalAmount")
    var totalAmount: TotalAmount
)

data class Authorisation(
    @field:Attribute(name = "CardPAN")
    @param:Attribute(name = "CardPAN")
    var cardPAN: String,
    @field:Attribute(name = "Merchant")
    @param:Attribute(name = "Merchant")
    var merchant: String,
    @field:Attribute(name = "TimeStamp")
    @param:Attribute(name = "TimeStamp")
    var timeStamp: String,
    @field:Attribute(name = "AcquirerID")
    @param:Attribute(name = "AcquirerID")
    var acquirerID: String,
    @field:Attribute(name = "ActionCode")
    @param:Attribute(name = "ActionCode")
    var actionCode: String,
    @field:Attribute(name = "ReturnCode")
    @param:Attribute(name = "ReturnCode")
    var returnCode: String,
    @field:Attribute(name = "CardCircuit")
    @param:Attribute(name = "CardCircuit")
    var cardCircuit: String,
    @field:Attribute(name = "ApprovalCode")
    @param:Attribute(name = "ApprovalCode")
    var approvalCode: String,
    @field:Attribute(name = "ReceiptNumber")
    @param:Attribute(name = "ReceiptNumber")
    var receiptNumber: String,
    @field:Attribute(name = "AuthorisationType")
    @param:Attribute(name = "AuthorisationType")
    var authorisationType: String,
)

data class CardDetails(
    @field:Element(name = "ExpiryDate")
    @param:Element(name = "ExpiryDate")
    var expiryDate: ValueElement
)

data class CardValue(
    @field:Element(name = "CardCircuit")
    @param:Element(name = "CardCircuit")
    var cardCircuit: ValueElement,
    @field:Element(name = "ExpiryDate")
    @param:Element(name = "ExpiryDate")
    var expiryDate: ValueElement,
    @field:Element(name = "CardPAN")
    @param:Element(name = "CardPAN")
    var CardPAN: ValueElement,
)

data class PrivateData(
    @field:Element(name = "CardHolderAuthentication")
    @param:Element(name = "CardHolderAuthentication")
    var cardHolderAuthentication: ValueElement,
)

data class ValueElement(
    @field:Text
    @param:Text
    var value: String,
)

enum class OverallResult(val value: String) {
    SUCCESS("Success"),
    PARTIAL_FAILURE("PartialFailure"),
    FAILURE("Failure"),
    DEVICE_UNAVAILABLE("DeviceUnavailable"),
    BUSY("Busy"),
    LOGGED_OUT("Loggedout"),
    ABORTED("Aborted"),
    TIMED_OUT("TimedOut"),
    FORMAT_ERROR("FormatError"),
    PARSING_ERROR("ParsingError"),
    COMMUNICATION_ERROR("CommunicationError"),
    VALIDATION_ERROR("ValidationError"),
    MISSING_MANDATORY_DATA("MissingMandatoryData"),
    UNKNOWN_CARD("UnknownCard"),
    UNKNOWN("Unknown"),
}