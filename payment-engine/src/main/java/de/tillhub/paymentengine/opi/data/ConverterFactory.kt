package de.tillhub.paymentengine.opi.data

class ConverterFactory {

    fun <T> newResponseConverter(clazz: Class<T>): ResponseConverter<T> =
        ResponseConverter(clazz)

    fun <T> newRequestConverter(): RequestConverter<T> = RequestConverter()
}