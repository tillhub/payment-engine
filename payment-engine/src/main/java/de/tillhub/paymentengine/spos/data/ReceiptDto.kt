package de.tillhub.paymentengine.spos.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Keep
@Root(name = "Receipt", strict = false)
internal data class ReceiptDto(
    @field:Attribute(name = "numCols")
    @param:Attribute(name = "numCols")
    var numCols: Int = 0,

    @field:ElementList(inline = true, required = false)
    @param:ElementList(inline = true, required = false)
    var receiptLines: List<ReceiptLineDto>? = null,
) {
    fun toReceiptString(): String {
        val sb = StringBuilder()

        receiptLines?.forEach { line ->
            line.text?.value?.let { sb.appendLine(it) }
        }

        return sb.toString()
    }
}

@Keep
@Root(name = "ReceiptLine", strict = false)
internal data class ReceiptLineDto(
    @field:Attribute(name = "type")
    @param:Attribute(name = "type")
    var type: String = "",

    @field:Element(name = "Formats", required = false)
    @param:Element(name = "Formats", required = false)
    var formats: FormatsDto? = null,
    @field:Element(name = "Text", required = false)
    @param:Element(name = "Text", required = false)
    var text: LineText? = null,
)

@Keep
@Root(name = "Formats", strict = false)
internal data class FormatsDto(
    @field:ElementList(inline = true, required = false)
    @param:ElementList(inline = true, required = false)
    var formats: List<FormatDto>? = null,
)

@Keep
@Root(name = "Format", strict = false)
internal data class FormatDto(
    @field:Attribute(name = "from")
    @param:Attribute(name = "from")
    var from: Int = 0,
    @field:Attribute(name = "to")
    @param:Attribute(name = "to")
    var to: Int = 0,

    @field:Text(required = false)
    @param:Text(required = false)
    var value: String? = null,
)

@Keep
@Parcelize
internal data class LineText(
    @field:Text(required = false)
    @param:Text(required = false)
    var value: String? = null,
) : Parcelable