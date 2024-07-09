package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Serializer
import retrofit2.Converter
import java.io.IOException

internal class StringToDtoConverter<T>(
    private val clazz: Class<T>,
    private val serializer: Serializer = OPISerializer()
) : Converter<String, T> {

    @Suppress("TooGenericExceptionThrown", "TooGenericExceptionCaught")
    override fun convert(value: String): T = try {
        val read: T = serializer.read(clazz, value, true)
            ?: throw IllegalStateException("Could not deserialize body as $clazz")
        read
    } catch (e: RuntimeException) {
        e.printStackTrace()
        throw e
    } catch (e: IOException) {
        e.printStackTrace()
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        throw RuntimeException(e)
    }
}