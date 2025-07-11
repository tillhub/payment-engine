package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class OPIChannelFactoryTest : FunSpec({
    val target = OPIChannelFactory()

    test("newOPIChannel0") {
        val result = target.newOPIChannel0("127.0.0.1", 20002)

        result.shouldBeInstanceOf<OPIChannel0>()
    }

    test("newOPIChannel1") {
        val result = target.newOPIChannel1(20007)

        result.shouldBeInstanceOf<OPIChannel1>()
    }
})