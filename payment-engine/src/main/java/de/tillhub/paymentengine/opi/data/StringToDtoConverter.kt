package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.Serializer
import retrofit2.Converter
import java.io.IOException

class StringToDtoConverter<T>(
    private val clazz: Class<T>,
    private val serializer: Serializer = OPISerializer()
) : Converter<String, T> {

    override fun convert(value: String): T? = try {
        val read: T = serializer.read(clazz, value, true)
            ?: throw IllegalStateException("Could not deserialize body as $clazz")
        read
    } catch (e: RuntimeException) {
        throw e
    } catch (e: IOException) {
        throw e
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}