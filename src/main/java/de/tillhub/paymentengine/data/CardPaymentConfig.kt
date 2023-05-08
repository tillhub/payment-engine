package de.tillhub.paymentengine.data

data class CardPaymentConfig(
    val integrationType: IntegrationType = DEFAULT_INTEGRATION_TYPE,
    val ipAddress: String = DEFAULT_IP,
    val port: Int = DEFAULT_PORT,
    val providerSpecificResultCodes: ResultCodeSets.ProviderSpecificResultCodes = DEFAULT_RESULT_CODES,
) {
    companion object {
        const val DEFAULT_IP = "127.0.0.1"
        const val DEFAULT_PORT = 40007 // CCV Fly 20007
        private val DEFAULT_INTEGRATION_TYPE = IntegrationType.ZVT
        const val LOCALHOST_IP = "127.0.0.1"
        const val NEXO_PORT = 5555
        private val DEFAULT_RESULT_CODES =
            ResultCodeSets.ProviderSpecificResultCodes.LavegoResultCodes
    }
}

enum class IntegrationType {
    ZVT, NEXO
}