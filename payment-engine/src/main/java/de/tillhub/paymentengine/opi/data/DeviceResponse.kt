package de.tillhub.paymentengine.opi.data

import androidx.annotation.Keep
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Keep
@Root(name = "DeviceResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
internal data class DeviceResponse(
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
    var workstationID: String = "",
    @field:Attribute(name = "OverallResult")
    @param:Attribute(name = "OverallResult")
    var overallResult: String = "",

    @field:ElementList(inline = true, required = false)
    @param:ElementList(inline = true, required = false)
    var output: List<Output>? = null,
)