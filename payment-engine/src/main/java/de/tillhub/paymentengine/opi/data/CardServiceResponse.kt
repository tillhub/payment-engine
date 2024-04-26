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

@Root(name = "Terminal", strict = false)
data class Terminal(
    @field:Attribute(name = "STAN", required = false)
    @param:Attribute(name = "STAN", required = false)
    var stan: String? = null,
    @field:Attribute(name = "TerminalID", required = false)
    @param:Attribute(name = "TerminalID", required = false)
    var terminalId: String? = null,
)

@Root(name = "Tender", strict = false)
data class Tender(
    @field:Element(name = "Authorisation", required = false)
    @param:Element(name = "Authorisation", required = false)
    var authorisation: Authorisation? = null,

    @field:Element(name = "TotalAmount", required = false)
    @param:Element(name = "TotalAmount", required = false)
    var totalAmount: TotalAmount? = null
)

@Root(name = "Authorisation", strict = false)
data class Authorisation(
    @field:Attribute(name = "CardPAN", required = false)
    @param:Attribute(name = "CardPAN", required = false)
    var cardPAN: String? = null,
    @field:Attribute(name = "Merchant", required = false)
    @param:Attribute(name = "Merchant", required = false)
    var merchant: String? = null,
    @field:Attribute(name = "TimeStamp", required = false)
    @param:Attribute(name = "TimeStamp", required = false)
    var timeStamp: String? = null,
    @field:Attribute(name = "AcquirerID", required = false)
    @param:Attribute(name = "AcquirerID", required = false)
    var acquirerID: String? = null,
    @field:Attribute(name = "ActionCode", required = false)
    @param:Attribute(name = "ActionCode", required = false)
    var actionCode: String? = null,
    @field:Attribute(name = "ReturnCode", required = false)
    @param:Attribute(name = "ReturnCode", required = false)
    var returnCode: String? = null,
    @field:Attribute(name = "CardCircuit", required = false)
    @param:Attribute(name = "CardCircuit", required = false)
    var cardCircuit: String? = null,
    @field:Attribute(name = "ApprovalCode", required = false)
    @param:Attribute(name = "ApprovalCode", required = false)
    var approvalCode: String? = null,
    @field:Attribute(name = "ReceiptNumber", required = false)
    @param:Attribute(name = "ReceiptNumber", required = false)
    var receiptNumber: String? = null,
    @field:Attribute(name = "AuthorisationType", required = false)
    @param:Attribute(name = "AuthorisationType", required = false)
    var authorisationType: String? = null,
)

@Root(name = "CardDetails", strict = false)
data class CardDetails(
    @field:Element(name = "ExpiryDate")
    @param:Element(name = "ExpiryDate")
    var expiryDate: ValueElement
)

@Root(name = "CardValue", strict = false)
data class CardValue(
    @field:Element(name = "CardCircuit", required = false)
    @param:Element(name = "CardCircuit")
    var cardCircuit: ValueElement? = null,
    @field:Element(name = "ExpiryDate", required = false)
    @param:Element(name = "ExpiryDate", required = false)
    var expiryDate: ValueElement? = null,
    @field:Element(name = "CardPAN", required = false)
    @param:Element(name = "CardPAN", required = false)
    var cardPAN: ValueElement? = null,
)

@Root(name = "PrivateData", strict = false)
data class PrivateData(
    @field:Element(name = "CardHolderAuthentication", required = false)
    @param:Element(name = "CardHolderAuthentication", required = false)
    var cardHolderAuthentication: ValueElement? = null,
    @field:Element(name = "CardTechnologyType", required = false)
    @param:Element(name = "CardTechnologyType", required = false)
    var cardTechnologyType: ValueElement? = null,
    @field:Element(name = "CardVerificationMethod", required = false)
    @param:Element(name = "CardVerificationMethod", required = false)
    var cardVerificationMethod: ValueElement? = null,
    @field:Element(name = "SoftwareVersion", required = false)
    @param:Element(name = "SoftwareVersion", required = false)
    var softwareVersion: ValueElement? = null,
    @field:Element(name = "ErrorCode", required = false)
    @param:Element(name = "ErrorCode", required = false)
    var errorCode: ValueElement? = null,
)

data class ValueElement(
    @field:Text(required = false)
    @param:Text(required = false)
    var value: String? = null,
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
    UNKNOWN("Unknown");

    companion object {
        fun find(value: String): OverallResult =
            entries.find { it.value == value } ?: UNKNOWN
    }
}