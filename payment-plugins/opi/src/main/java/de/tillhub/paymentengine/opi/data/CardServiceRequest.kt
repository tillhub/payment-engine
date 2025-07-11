package de.tillhub.paymentengine.opi.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text
import java.math.BigDecimal

@Keep
@Root(name = "CardServiceRequest", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
internal data class CardServiceRequest(
    @field:Attribute(name = "ApplicationSender")
    @param:Attribute(name = "ApplicationSender")
    var applicationSender: String = "",
    @field:Attribute(name = "POPID")
    @param:Attribute(name = "POPID")
    var popId: String = "",
    @field:Attribute(name = "RequestID")
    @param:Attribute(name = "RequestID")
    var requestId: String = "",
    @field:Attribute(name = "RequestType")
    @param:Attribute(name = "RequestType")
    var requestType: String = "",
    @field:Attribute(name = "WorkstationID")
    @param:Attribute(name = "WorkstationID")
    var workstationId: String = "",

    @field:Element(name = "POSdata", required = false)
    @param:Element(name = "POSdata", required = false)
    var posData: PosData? = null,

    @field:Element(name = "TotalAmount", required = false)
    @param:Element(name = "TotalAmount", required = false)
    var totalAmount: TotalAmount? = null,

    @field:Element(name = "OriginalTransaction", required = false)
    @param:Element(name = "OriginalTransaction", required = false)
    var originalTransaction: OriginalTransaction? = null,
)

@Keep
@Root(name = "POSdata", strict = false)
internal data class PosData(
    @field:Element(name = "POSTimeStamp")
    @param:Element(name = "POSTimeStamp")
    var timestamp: String = ""
)

@Keep
@Parcelize
@Root(name = "TotalAmount", strict = false)
internal data class TotalAmount(
    @field:Text
    @param:Text
    var value: BigDecimal = BigDecimal.ZERO,
    @field:Attribute(name = "Currency")
    @param:Attribute(name = "Currency")
    var currency: String = "",

    @field:Attribute(name = "CardCircuit", required = false)
    @param:Attribute(name = "CardCircuit", required = false)
    var cardCircuit: String? = null,
    @field:Attribute(name = "NumberPayments", required = false)
    @param:Attribute(name = "NumberPayments", required = false)
    var numberPayments: Int? = null,
    @field:Attribute(name = "PaymentType", required = false)
    @param:Attribute(name = "PaymentType", required = false)
    var paymentType: String? = null,
    @field:Attribute(name = "PaymentAmount", required = false)
    @param:Attribute(name = "PaymentAmount", required = false)
    var paymentAmount: String? = null,
) : Parcelable

@Keep
internal data class OriginalTransaction(
    @field:Attribute(name = "STAN")
    @param:Attribute(name = "STAN")
    var stan: String = "",
)

internal enum class ServiceRequestType(val value: String) {
    CARD_PAYMENT("CardPayment"),
    PAYMENT_REVERSAL("PaymentReversal"),
    PAYMENT_REFUND("PaymentRefund"),
    RECONCILIATION("ReconciliationWithClosure"),
    LOGIN("Login"),
    ABORT_REQUEST("AbortRequest"),
}

/***
 * XML examples
 *
 * Payment request:
 * <?xml version="1.0" encoding="ISO-8859-1" ?>
 * <CardServiceRequest
 *   ApplicationSender="SECposPay"
 *   POPID="001"
 *   RequestID="0"
 *   RequestType="CardPayment"
 *   WorkstationID=""
 *   xmlns="http://www.nrf-arts.org/IXRetail/namespace"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema- instance">
 * 	    <POSdata>
 * 		    <POSTimeStamp>2007-05-23T13:19:39</POSTimeStamp>
 * 	    </POSdata>
 * 	    <TotalAmount Currency="EUR">120.00</TotalAmount>
 * </CardServiceRequest>
 *
 *
 * Payment reversal request:
 *
 * <?xml version="1.0" encoding="ISO-8859-1" ?>.
 * <CardServiceRequest
 *   ApplicationSender="SECposPay"
 *   POPID="001" RequestID="0"
 *   RequestType="PaymentReversal"
 *   WorkstationID=""
 *   xmlns="http://www.nrf-arts.org/IXRetail/namespace"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema- instance">
 * 	    <POSdata>
 * 		    <POSTimeStamp>2007-05-23T13:24:19</POSTimeStamp>
 * 	    </POSdata>
 * 	    <OriginalTransaction STAN="29" />
 * </CardServiceRequest>
 *
 * Payment refund request:
 *
 * <?xml version="1.0" encoding="ISO-8859-1" ?>.
 * <CardServiceRequest
 *   ApplicationSender="SECposPay"
 *   POPID="001"
 *   RequestID="0"
 *   RequestType="PaymentRefund"
 *   WorkstationID=""
 *   xmlns="http://www.nrf-arts.org/IXRetail/namespace"
 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema- instance">
 * 	    <POSdata>
 * 		    <POSTimeStamp>2007-05- 23T13:34:37</POSTimeStamp>
 * 	    </POSdata>
 * 	    <TotalAmount Currency="EUR">15.00</TotalAmount>
 * </CardServiceRequest>
 *
 */