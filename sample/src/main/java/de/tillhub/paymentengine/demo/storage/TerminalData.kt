package de.tillhub.paymentengine.demo.storage

import androidx.datastore.core.okio.OkioSerializer
import de.tillhub.paymentengine.demo.di.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import okio.use


internal object TerminalDataSerializer : OkioSerializer<TerminalData> {
    override val defaultValue: TerminalData
        get() = TerminalData()

    override suspend fun readFrom(source: BufferedSource): TerminalData {
        return json.decodeFromString<TerminalData>(source.readUtf8())
    }

    override suspend fun writeTo(t: TerminalData, sink: BufferedSink) {
        sink.use {
            it.writeUtf8(json.encodeToString(t))
        }
    }
}

@Serializable
data class TerminalData(
    @SerialName("ip_address") val ipAddress: String? = "",
    @SerialName("port1") val port1: String? = "",
    @SerialName("port2") val port2: String? = "",
)
