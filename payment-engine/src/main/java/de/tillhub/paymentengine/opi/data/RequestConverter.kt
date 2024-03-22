package de.tillhub.paymentengine.opi.data

import okio.Buffer
import org.simpleframework.xml.Serializer
import retrofit2.Converter
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class RequestConverter<T>(
    private val serializer: Serializer = OPISerializer()
) : Converter<T, String> {

    override fun convert(value: T): String {
        val buffer = Buffer()
        try {
            val osw = OutputStreamWriter(buffer.outputStream(), CHARSET)
            serializer.write(value, osw)
            osw.flush()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return buffer.readString(Charset.forName(CHARSET))
    }

    companion object {
        private const val CHARSET = "UTF-8"
    }
}