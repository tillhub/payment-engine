package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

@ExperimentalCoroutinesApi
class OPIChannel1Test : FunSpec({
    val socketPort: Int = 20007
    lateinit var socketFactory: SocketFactory
    lateinit var coroutineScope: CoroutineScope

    lateinit var serverSocket: ServerSocket
    lateinit var socket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream
    lateinit var callbacks: Callbacks

    lateinit var target: OPIChannel1

    beforeAny {
        inputStream = mockk {
            every { available() } returns 0
            every { close() } just Runs
        }
        outputStream = mockk {
            every { close() } just Runs
            every { flush() } just Runs
            every { write(any<Int>()) } just Runs
        }
        callbacks = mockk {
            every { onMessage(any()) } just Runs
            every { onError(any(), any()) } just Runs
        }
        socket = mockk {
            every { isConnected } returns true
            every { isClosed } returns false
            every { close() } just Runs
            every { getInputStream() } returns inputStream
            every { getOutputStream() } returns outputStream
        }
        serverSocket = mockk {
            every { accept() } returns socket
            every { isClosed } returns false
            every { isBound } returns true
            every { close() } just Runs
        }

        socketFactory = mockk {
            every { createServerSocket(any()) } returns serverSocket
        }
        coroutineScope = TestScope(UnconfinedTestDispatcher())

        target = OPIChannel1(socketPort, socketFactory, coroutineScope)
        target.setOnError(callbacks::onError)
    }

    test("open") {
        target.isConnected shouldBe false

        target.open()

        target.isConnected shouldBe true

        verify(Ordering.ORDERED) {
            socketFactory.createServerSocket(socketPort)
        }

        target.close()

        target.isConnected shouldBe false
    }

    test("") {

    }
}) {
    interface Callbacks {
        fun onMessage(s: String)
        fun onError(t: Throwable, s: String)
    }
}