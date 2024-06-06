package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.net.ServerSocket
import java.net.Socket

@ExperimentalCoroutinesApi
class SocketFactoryTest : FunSpec({
    val target = SocketFactory()

    test("createServerSocket") {
        val result = target.createServerSocket(20002)
        result.shouldBeInstanceOf<ServerSocket>()
    }

    test("createClientSocket") {
        val result = target.createClientSocket("127.0.0.1", 20002)
        result.shouldBeInstanceOf<Socket>()
    }
})