package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

@ExperimentalCoroutinesApi
class OPIChannel1Test : FunSpec({
    val socketPort = 20007
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
        target.setOnMessage(callbacks::onMessage)
    }

    test("open & close") {
        target.isConnected shouldBe false

        target.open()

        target.isConnected shouldBe true

        verify(Ordering.ORDERED) {
            socketFactory.createServerSocket(socketPort)
        }

        target.close()

        target.isConnected shouldBe false
    }

    test("sendMessage") {
        val payload = RESPONSE_SIZE_BYTES + RESPONSE_XML_BYTES

        target.open()

        coroutineScope.launch {
            target.sendMessage(RESPONSE_XML)

            verify {
                payload.forEach {
                    outputStream.write(it.toInt())
                }
                outputStream.flush()
            }

            target.close()
        }
    }

    test("receive full message") {
        target.open()

        every { inputStream.available() } returns (REQUEST_SIZE_BYTES + REQUEST_XML_BYTES).size

        coroutineScope.launch {
            coVerify {
                callbacks.onMessage(REQUEST_XML)
            }
        }
    }

    test("receive partial message") {
        val spliceLoc = REQUEST_XML_BYTES.size / 2
        val part1 = REQUEST_SIZE_BYTES + REQUEST_XML_BYTES.slice(IntRange(0, spliceLoc - 1))
        val part2 = REQUEST_XML_BYTES
            .slice(IntRange(spliceLoc, REQUEST_XML_BYTES.size - 1)).toByteArray()

        var callCount = 0
        every { inputStream.read(any(), any(), any()) } answers {
            val arr = firstArg<ByteArray>()

            callCount++

            when (callCount) {
                1 -> {
                    every { inputStream.available() } returns part2.size

                    part1.copyInto(arr)
                    part1.size
                }
                2 -> {
                    every { inputStream.available() } returns 0

                    part2.copyInto(arr)
                    part2.size
                }
                else -> 0
            }
        }

        target.open()

        every { inputStream.available() } returns part1.size

        coroutineScope.launch {
            coVerify {
                callbacks.onMessage(REQUEST_XML)
            }
        }
    }
}) {
    companion object {
        val RESPONSE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <DeviceResponse ApplicationSender="WINPOS" OverallResult="Success"
            	xmlns="http://www.nrf-arts.org/IXRetail/namespace" RequestID="20792" WorkstationID="1"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" POPID="001" RequestType="Output">
            	<Output OutDeviceTarget="CashierDisplay" OutResult="Success"/>
            </DeviceResponse>
        """.trimIndent()
        val RESPONSE_XML_BYTES = RESPONSE_XML.toByteArray()
        val RESPONSE_SIZE_BYTES = ByteBuffer.allocate(4)
            .putInt(RESPONSE_XML_BYTES.size).array()

        val REQUEST_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <DeviceRequest RequestType="Output" RequestID="20792" WorkstationID="1" SequenceID="2" TerminalID="68276185" ApplicationSender="Thales-OPI"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="file:///C:/Windows/O.P.I.Schema/DeviceRequest.xsd">
            	<Output OutDeviceTarget="CashierDisplay">
            		<TextLine Column="0" Row="0">Vorgang wird</TextLine>
            		<TextLine Column="0" Row="1">bearbeitet</TextLine>
            	</Output>
            </DeviceRequest>
        """.trimIndent()
        val REQUEST_XML_BYTES = REQUEST_XML.toByteArray()
        val REQUEST_SIZE_BYTES = ByteBuffer.allocate(4)
            .putInt(REQUEST_XML_BYTES.size).array()
    }
    interface Callbacks {
        fun onMessage(s: String)
        fun onError(t: Throwable, s: String)
    }
}