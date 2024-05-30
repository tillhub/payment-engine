package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer

@ExperimentalCoroutinesApi
class OPIChannel0Test : FunSpec({
    val socketIp = "127.0.0.1"
    val socketPort = 20002
    lateinit var socketFactory: SocketFactory
    lateinit var coroutineScope: CoroutineScope

    lateinit var socket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream
    lateinit var callbacks: Callbacks

    lateinit var target: OPIChannel0

    beforeAny {
        inputStream = mockk {
            every { available() } returns 0
            every { read(any(), any(), any()) } answers {
                val data = RESPONSE_SIZE_BYTES + CARD_SERVICE_RESPONSE_BYTES
                val arr = firstArg<ByteArray>()

                data.copyInto(arr)
                data.size
            }
            every { close() } just Runs
        }
        outputStream = mockk {
            every { close() } just Runs
            every { flush() } just Runs
            every { write(any<Int>()) } just Runs
        }
        callbacks = spyk(object : Callbacks {
            override fun onMessage(s: String) {
                every { inputStream.available() } returns 0
            }

            override fun onError(t: Throwable, s: String) = Unit

        })
        socket = mockk {
            every { isConnected } returns true
            every { isClosed } returns false
            every { close() } just Runs
            every { getInputStream() } returns inputStream
            every { getOutputStream() } returns outputStream
        }

        socketFactory = mockk {
            every { createClientSocket(any(), any()) } returns socket
        }
        coroutineScope = spyk(TestScope(UnconfinedTestDispatcher()))

        target = OPIChannel0(socketIp, socketPort, socketFactory, coroutineScope)
        target.setOnError(callbacks::onError)
    }

    test("open & close") {
        target.isConnected shouldBe false

        target.open()

        target.isConnected shouldBe true

        verify(Ordering.ORDERED) {
            socketFactory.createClientSocket(socketIp, socketPort)
            socket.getInputStream()
            socket.getOutputStream()
            inputStream.available()
        }

        target.close()

        target.isConnected shouldBe false
    }

    test("send request and receive full response") {
        target.open()

        target.sendMessage(CARD_SERVICE_REQUEST, callbacks::onMessage)

        every { inputStream.available() } returns
                (RESPONSE_SIZE_BYTES + CARD_SERVICE_RESPONSE_BYTES).size

        coroutineScope.launch {
            coVerify {
                callbacks.onMessage(CARD_SERVICE_RESPONSE)
            }
        }
        target.close()
    }

    test("send request and receive partial response") {
        val spliceLoc = CARD_SERVICE_RESPONSE_BYTES.size/2
        val part1 = RESPONSE_SIZE_BYTES + CARD_SERVICE_RESPONSE_BYTES.slice(IntRange(0, spliceLoc-1))
        val part2 = CARD_SERVICE_RESPONSE_BYTES
            .slice(IntRange(spliceLoc, CARD_SERVICE_RESPONSE_BYTES.size-1)).toByteArray()

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

        target.sendMessage(CARD_SERVICE_REQUEST, callbacks::onMessage)

        every { inputStream.available() } returns part1.size

        coroutineScope.launch {
            coVerify {
                callbacks.onMessage(CARD_SERVICE_RESPONSE)
            }
        }

        target.close()
    }
}) {
    companion object {
        private val CARD_SERVICE_REQUEST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <CardServiceRequest RequestType="CardPayment" ApplicationSender="TillhubIOS" POPID="001"
            	xmlns="http://www.nrf-arts.org/IXRetail/namespace" RequestID="20544" WorkstationID="1">
            	<POSdata LanguageCode="de" RequestFullPAN="true">
            		<POSTimeStamp>2024-02-09T09:36:36</POSTimeStamp>
            	</POSdata>
            	<TotalAmount Currency="EUR">39.50</TotalAmount>
            </CardServiceRequest>
        """.trimIndent()

        private val CARD_SERVICE_REQUEST_BYTES = CARD_SERVICE_REQUEST.toByteArray()

        private val CARD_SERVICE_RESPONSE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <CardServiceResponse
            	xmlns="http://www.nrf-arts.org/IXRetail/namespace"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.nrf-arts.org/IXRetail/namespace file:///C:/Windows/O.P.I.Schema/CardResponse.xsd" RequestType="CardPayment" WorkstationID="1" RequestID="20544" OverallResult="Success">
            	<Terminal TerminalID="68276185" STAN="021601"/>
            	<Tender>
            		<TotalAmount Currency="EUR">39.50</TotalAmount>
            		<Authorisation AuthorisationType="Online" CardCircuit="girocard" CardPAN="6805330010086784747" Merchant="VU-123456789017" ReceiptNumber="8930" ActionCode="0" TimeStamp="2024-02-09T09:36:28-00:00" AcquirerID="undefined"/>
            	</Tender>
            	<PrivateData>
            		<CardTechnologyType>NFC</CardTechnologyType>
            		<CardVerificationMethod>NoCVM</CardVerificationMethod>
            		<ErrorCode>1002700</ErrorCode>
            		<ErrorText>Zahlung erfolgt</ErrorText>
            	</PrivateData>
            	<CardValue>
            		<ExpiryDate>1227</ExpiryDate>
            	</CardValue>
            </CardServiceResponse>
        """.trimIndent()

        private val CARD_SERVICE_RESPONSE_BYTES = CARD_SERVICE_RESPONSE.toByteArray()
        private val RESPONSE_SIZE_BYTES = ByteBuffer.allocate(4)
            .putInt(CARD_SERVICE_RESPONSE_BYTES.size).array()
    }

    interface Callbacks {
        fun onMessage(s: String)
        fun onError(t: Throwable, s: String)
    }
}