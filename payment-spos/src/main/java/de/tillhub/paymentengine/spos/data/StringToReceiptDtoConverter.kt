package de.tillhub.paymentengine.spos.data

import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import retrofit2.Converter
import java.io.IOException

internal class StringToReceiptDtoConverter(
    private val serializer: Serializer = Persister()
) : Converter<String, ReceiptDto> {

    @Suppress("TooGenericExceptionThrown", "TooGenericExceptionCaught")
    override fun convert(value: String): ReceiptDto = try {
        val read: ReceiptDto = serializer.read(ReceiptDto::class.java, value, true)
            ?: throw IllegalStateException("Could not deserialize body as ReceiptDto")
        read
    } catch (e: RuntimeException) {
        throw e
    } catch (e: IOException) {
        throw e
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}