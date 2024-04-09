package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "DeviceRequest", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
data class DeviceRequest(
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

    @field:Element(name = "Output", required = false)
    @param:ElementList(name = "Output", required = false)
    var output: List<Output>? = null,
)

data class Output(
    @field:Attribute(name = "OutDeviceTarget")
    @param:Attribute(name = "OutDeviceTarget")
    var outDeviceTarget: String,

    @field:Attribute(name = "OutResult", required = false)
    @param:Attribute(name = "OutResult", required = false)
    var outResult: String? = null,

    @field:ElementList(name = "TextLine", required = false)
    @param:ElementList(name = "TextLine", required = false)
    var textLines: List<TextLine>? = null
)

data class TextLine(
    @field:Attribute(name = "TimeOut", required = false)
    @param:Attribute(name = "TimeOut", required = false)
    var timeOut: Int? = null,

    @field:Attribute(name = "Hidden", required = false)
    @param:Attribute(name = "Hidden", required = false)
    var hidden: Boolean = false,

    @field:Attribute(name = "Column", required = false)
    @param:Attribute(name = "Column", required = false)
    var column: Int? = null,

    @field:Attribute(name = "Row", required = false)
    @param:Attribute(name = "Row", required = false)
    var row: Int? = null,

    @field:Text(required = false)
    @param:Text(required = false)
    var value: String? = null
)

enum class DeviceType(val value: String) {
    CASHIER_DISPLAY("CashierDisplay"),
    CUSTOMER_DISPLAY("CustomerDisplay"),
    PRINTER("Printer"),
    PRINTER_RECEIPT("PrinterReceipt"),
    CASHIER_KEYBOARD("CashierKeyboard"),
    CASHIER_TERMINAL("CashierTerminal"),
    UNKNOWN(""),
}

enum class DeviceRequestType(val value: String) {
    OUTPUT("Output"),
    INPUT("Input")
}