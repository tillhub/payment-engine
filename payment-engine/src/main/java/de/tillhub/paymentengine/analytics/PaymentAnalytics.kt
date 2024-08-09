package de.tillhub.paymentengine.analytics

interface PaymentAnalytics {
    fun logOperation(request: String)
    fun logCommunication(protocol: String, message: String)
}