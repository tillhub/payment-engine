package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "ServiceResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
data class ServiceResponse(
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

    @field:Element(name = "PrivateData", required = false)
    @param:Element(name = "PrivateData", required = false)
    var privateData: PrivateData? = null,
)