package de.tillhub.paymentengine.opi.communication

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

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
            every { available() } returns CARD_SERVICE_RESPONSE_BYTES.size
            every { read(any(), any(), any()) } answers {
                val arr = firstArg<ByteArray>()
                CARD_SERVICE_RESPONSE_BYTES.copyInto(arr)

                CARD_SERVICE_RESPONSE_BYTES.size
            }
            every { close() } just Runs
        }
        outputStream = mockk {
            every { close() } just Runs
            every { flush() } just Runs
            every { write(any<ByteArray>()) } just Runs
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

        socketFactory = mockk {
            every { createClientSocket(any(), any()) } returns socket
        }
        coroutineScope = spyk(TestScope(UnconfinedTestDispatcher()))

        target = OPIChannel0(socketIp, socketPort, socketFactory, coroutineScope)
        target.setOnError(callbacks::onError)
    }

    test("open") {
        target.isConnected shouldBe false

        target.open()


    }
}) {
    companion object {
        private val CARD_SERVICE_REQUEST = """
            <?xml version="1.0" encoding="UTF-8" ?>
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
            <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
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
    }

    interface Callbacks {
        fun onMessage(s: String)
        fun onError(t: Throwable, s: String)
    }
}