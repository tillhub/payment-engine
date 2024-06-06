package de.tillhub.paymentengine.opi.data

internal class ConverterFactory {

    fun <T> newStringToDtoConverter(clazz: Class<T>): StringToDtoConverter<T> =
        StringToDtoConverter(clazz)

    fun <T> newDtoToStringConverter(): DtoToStringConverter<T> = DtoToStringConverter()
}