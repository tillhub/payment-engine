package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text
import java.math.BigDecimal

@Root(name = "CardServiceRequest", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
data class CardServiceRequest(
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

data class PosData(
    @field:Element(name = "POSTimeStamp")
    @param:Element(name = "POSTimeStamp")
    var timestamp: String
)

@Root(name = "TotalAmount", strict = false)
data class TotalAmount(
    @field:Text
    @param:Text
    var value: BigDecimal,
    @field:Attribute(name = "Currency")
    @param:Attribute(name = "Currency")
    var currency: String,

    @field:Attribute(name = "PaymentAmount", required = false)
    @param:Attribute(name = "PaymentAmount", required = false)
    var paymentAmount: String? = null,
)

data class OriginalTransaction(
    @field:Attribute(name = "STAN")
    @param:Attribute(name = "STAN")
    var stan: String,
)

enum class CardServiceRequestType(val value: String) {
    CARD_PAYMENT("CardPayment"),
    PAYMENT_REVERSAL("PaymentReversal"),
    PAYMENT_REFUND("PaymentRefund"),
    RECONCILIATION("ReconciliationWithClosure");
}