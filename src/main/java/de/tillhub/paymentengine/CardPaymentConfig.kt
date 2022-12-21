package de.tillhub.paymentengine


class CardPaymentConfig {

    var integrationType: IntegrationType = DEFAULT_INTEGRATION_TYPE
    var ipAddress: String = DEFAULT_IP
    var port: Int = DEFAULT_PORT
    var providerSpecificResultCodes: ResultCodeSets.ProviderSpecificResultCodes = DEFAULT_RESULT_CODES

    companion object {
        const val DEFAULT_IP = "127.0.0.1"
        const val DEFAULT_PORT = 40007 // CCV Fly 20007
        private val DEFAULT_INTEGRATION_TYPE = IntegrationType.NEXO
        const val LOCALHOST_IP = "127.0.0.1"
        const val NEXO_PORT = 5555
        private val DEFAULT_RESULT_CODES = ResultCodeSets.ProviderSpecificResultCodes.LavegoResultCodes
    }
}

enum class IntegrationType {
    ZVT, NEXO
}
