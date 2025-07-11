package de.tillhub.paymentengine.opi.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ConverterFactoryTest : FunSpec({
    val target = ConverterFactory()

    test("newStringToDtoConverter") {
        val result1 = target.newStringToDtoConverter(
            clazz = ServiceResponse::class.java
        )
        val result2 = target.newStringToDtoConverter(
            clazz = CardServiceResponse::class.java
        )
        val result3 = target.newStringToDtoConverter(
            clazz = DeviceRequest::class.java
        )

        result1.shouldBeInstanceOf<StringToDtoConverter<ServiceResponse>>()
        result2.shouldBeInstanceOf<StringToDtoConverter<CardServiceResponse>>()
        result3.shouldBeInstanceOf<StringToDtoConverter<DeviceRequest>>()
    }

    test("newDtoToStringConverter") {
        val result1 = target.newDtoToStringConverter<ServiceRequest>()
        val result2 = target.newDtoToStringConverter<CardServiceRequest>()
        val result3 = target.newDtoToStringConverter<DeviceResponse>()

        result1.shouldBeInstanceOf<DtoToStringConverter<ServiceRequest>>()
        result2.shouldBeInstanceOf<DtoToStringConverter<CardServiceRequest>>()
        result3.shouldBeInstanceOf<DtoToStringConverter<DeviceResponse>>()
    }
})