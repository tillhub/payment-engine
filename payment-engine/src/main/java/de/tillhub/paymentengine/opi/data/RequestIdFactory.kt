package de.tillhub.paymentengine.opi.data

import kotlin.random.Random

internal class RequestIdFactory {
    fun generateRequestId(): String {
        val chars = "1234567890"
        val idBuilder = StringBuilder()
        while (idBuilder.length < REQUEST_ID_LENGTH) {
            idBuilder.append(chars[(Random.nextFloat() * chars.length).toInt()])
        }
        return idBuilder.toString()
    }

    companion object {
        private const val REQUEST_ID_LENGTH = 8
    }
}