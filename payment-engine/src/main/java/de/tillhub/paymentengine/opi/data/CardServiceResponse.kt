package de.tillhub.paymentengine.opi.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Parcelize
@Root(name = "CardServiceResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
internal data class CardServiceResponse(
    @field:Attribute(name = "RequestID")
    @param:Attribute(name = "RequestID")
    var requestId: String,
    @field:Attribute(name = "RequestType")
    @param:Attribute(name = "RequestType")
    var requestType: String,
    @field:Attribute(name = "WorkstationID")
    @param:Attribute(name = "WorkstationID")
    var workstationId: String,
    @field:Attribute(name = "OverallResult")
    @param:Attribute(name = "OverallResult")
    override var overallResult: String,

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
) : Parcelable, OPIResponse

@Parcelize
@Root(name = "Terminal", strict = false)
internal data class Terminal(
    @field:Attribute(name = "STAN", required = false)
    @param:Attribute(name = "STAN", required = false)
    var stan: String? = null,
    @field:Attribute(name = "TerminalID", required = false)
    @param:Attribute(name = "TerminalID", required = false)
    var terminalId: String? = null,
) : Parcelable

@Parcelize
@Root(name = "Tender", strict = false)
internal data class Tender(
    @field:Element(name = "Authorisation", required = false)
    @param:Element(name = "Authorisation", required = false)
    var authorisation: Authorisation? = null,

    @field:Element(name = "TotalAmount", required = false)
    @param:Element(name = "TotalAmount", required = false)
    var totalAmount: TotalAmount? = null
) : Parcelable

@Parcelize
@Root(name = "Authorisation", strict = false)
internal data class Authorisation(
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
) : Parcelable

@Parcelize
@Root(name = "CardDetails", strict = false)
internal data class CardDetails(
    @field:Element(name = "ExpiryDate")
    @param:Element(name = "ExpiryDate")
    var expiryDate: ValueElement
) : Parcelable

@Parcelize
@Root(name = "CardValue", strict = false)
internal data class CardValue(
    @field:Element(name = "CardCircuit", required = false)
    @param:Element(name = "CardCircuit")
    var cardCircuit: ValueElement? = null,
    @field:Element(name = "ExpiryDate", required = false)
    @param:Element(name = "ExpiryDate", required = false)
    var expiryDate: ValueElement? = null,
    @field:Element(name = "CardPAN", required = false)
    @param:Element(name = "CardPAN", required = false)
    var cardPAN: ValueElement? = null,
) : Parcelable

@Parcelize
@Root(name = "PrivateData", strict = false)
internal data class PrivateData(
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
    @field:Element(name = "RebootInfo", required = false)
    @param:Element(name = "RebootInfo", required = false)
    var rebootInfo: ValueElement? = null,
) : Parcelable

@Parcelize
internal data class ValueElement(
    @field:Text(required = false)
    @param:Text(required = false)
    var value: String? = null,
) : Parcelable

internal enum class OverallResult(val value: String) {
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

/***
 * XML examples
 *
 * Card payment response:
 *
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <CardServiceResponse
 *   POPID="001"
 *   xmlns="http://www.nrf- arts.org/IXRetail/namespace"
 *   RequestID="0"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   RequestType="CardPayment"
 *   OverallResult="Success"
 *   WorkstationID="Workstation"
 *   ApplicationSender="xPay"
 *   xsi:noNamespaceSchemaLocation="C:\Windows\OPISchema\CardResponse.xsd">
 * 	    <Terminal STAN="72" TerminalID="43100066" />
 * 	    <Tender>
 * 	    	<TotalAmount Currency="EUR" PaymentAmount="120.00">120.00</TotalAmount>
 * 	    	<Authorisation
 * 	    		CardPAN="672667#########3214"
 * 	    		Merchant="455600000599 "
 * 	    		TimeStamp="2011-07-20T10:50:55"
 * 		    	AcquirerID="431"
 * 		    	ActionCode="0"
 * 	    		ReturnCode="0"
 * 		    	CardCircuit="girocard"
 * 	    		ApprovalCode="00001111"
 * 		    	ReceiptNumber="25"
 * 	    		AuthorisationType="Online" />
 * 	    </Tender>
 *  	<CardDetails>
 * 	    	<ExpiryDate>12/13</ExpiryDate>
 *  	</CardDetails>
 *  	<CardValue>
 * 	    	<CardCircuit>girocard</CardCircuit>
 * 	    	<ExpiryDate>12/13</ExpiryDate>
 * 	    	<CardPAN>672667#########3214</CardPAN>
 *  	</CardValue>
 *  	<PrivateData>
 * 	    	<CardHolderAuthentication>PIN</CardHolderAuthentication>
 *  	</PrivateData>
 * </CardServiceResponse>
 *
 * Payment reversal response:
 *
 * <?xml version="1.0" encoding="ISO-8859-1" ?>.
 * <CardServiceResponse
 *   ApplicationSender="SECposPay"
 *   OverallResult="Success"
 *   POPID="001"
 *   RequestID="0"
 *   RequestType="PaymentReversal"
 *   WorkstationID=""
 *   xmlns="http://www.nrf-arts.org/IXRetail/namespace"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xsi:noNamespaceSchemaLocation="C:\Windows\OPISchema\CardResponse.xsd">
 * 	    <Terminal STAN="30" TerminalID="75757575" />
 * 	    <Tender>
 * 		    <Authorisation
 * 		        AcquirerID="757"
 * 		        ActionCode="0"
 * 		        AIDParameter=""
 * 		        AuthorisationType="online"
 * 		        CardCircuit="GermanDebitEcCash"
 *              CardPAN="XXXXXXXXXX=XXXXXXXXXXX"
 *              ReceiptNumber="7"
 *              ReturnCode="0"
 *              TimeStamp="2007-05-23T13:23:46" />
 * 		    <TotalAmount Currency="EUR" PaymentAmount="25.00">25.00</TotalAmount>
 * 	    </Tender>
 * 	    <CardDetails>
 * 		    <ExpiryDate>12/8</ExpiryDate>
 * 			<CurrencyCode>280</CurrencyCode>
 * 			<SequenceNumber>0</SequenceNumber>
 * 		</CardDetails>
 * 		<CardValue>
 * 			<CardCircuit>GermanDebitEcCash</CardCircuit>
 * 			<ExpiryDate>1 2/8</ExpiryDate>
 * 			<Track1></Track1>
 * 			<Track2></Track2>
 * 			<Track3></Track3>
 * 			<CardPAN>XXXX XXXXXX=XXXXXXXXXXX</CardPAN>
 * 		</CardValue>
 * 	</CardServiceResponse>
 *
 * 	Payment refund response:
 *
 * 	<?xml version="1.0" encoding="ISO-8859-1" ?>.
 * <CardServiceResponse
 *   ApplicationSender="SECposPay"
 *   OverallResult="Success"
 *   POPID="001"
 *   RequestID="0"
 *   RequestType="PaymentRefund"
 *   WorkstationID=""
 *   xmlns="http://www.nrf-arts.org/IXRetail/namespace"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *   xsi:noNamespaceSchemaLocation="C:\Windows\OPISchema\CardResponse.xsd">
 * 	    <Terminal STAN="32" TerminalID="75757575" />
 * 	    <Tender>
 * 		    <Authorisation
 * 			    AcquirerID="757"
 * 			    ActionCode="0"
 * 			    ApprovalCode="00001111"
 * 		    	AuthorisationType="online"
 * 	    		CardCircuit="VISA"
 * 	    		CardPAN="XXXXXXXXXXXXXXXX"
 * 		    	ReceiptNumber="9"
 * 		    	ReturnCode="0"
 * 		    	TimeStamp="2007-05-23T13:34:11" />
 * 		    <TotalAmount Currency="EUR" PaymentAmount="15.00">15.00</TotalAmount>
 * 	    </Tender>
 * 	    <CardDetails>
 * 		    <ExpiryDate>12/10 </ExpiryDate>
 * 	    </CardDetails>
 * 	    <CardValue>
 * 		    <CardCircuit>VISA</CardCircuit>
 * 	    	<ExpiryDate >12/10</ExpiryDate>
 * 		    <Track1>%BXXXXXXXXXXXXXXXX^OPEN HIGH LIMIT ^10121010XXXXXXXXXXXXXX000000000?</Track1>
 * 	    	<Track2>;XXXXXXXXXXXXXXXX=101210100000 XXXXXXXX?</Track2>
 * 	    	<Track3></Track3>
 * 	    	<CardPAN>XXXXXXXXXXXXXXXX</CardPAN>
 * 	    </CardValue>
 * </CardServiceResponse>
 */