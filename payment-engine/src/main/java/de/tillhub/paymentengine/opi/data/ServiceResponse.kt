package de.tillhub.paymentengine.opi.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Keep
@Parcelize
@Root(name = "ServiceResponse", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.nrf-arts.org/IXRetail/namespace"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
)
internal data class ServiceResponse(
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
    override var overallResult: String = "",

    @field:Element(name = "Terminal", required = false)
    @param:Element(name = "Terminal", required = false)
    var terminal: Terminal? = null,

    @field:Element(name = "PrivateData", required = false)
    @param:Element(name = "PrivateData", required = false)
    var privateData: PrivateData? = null,

    @field:Element(name = "Authorisation", required = false)
    @param:Element(name = "Authorisation", required = false)
    var authorisation: Authorisation? = null,

    @field:Element(name = "Reconciliation", required = false)
    @param:Element(name = "Reconciliation", required = false)
    var reconciliation: Reconciliation? = null,
) : Parcelable, OPIResponse

@Parcelize
@Root(name = "Reconciliation", strict = false)
internal data class Reconciliation(
    @field:Attribute(name = "LanguageCode", required = false)
    @param:Attribute(name = "LanguageCode", required = false)
    var languageCode: String? = null,

    @field:ElementList(inline = true, required = false)
    @param:ElementList(inline = true, required = false)
    var totalAmounts: List<TotalAmount>? = null
) : Parcelable