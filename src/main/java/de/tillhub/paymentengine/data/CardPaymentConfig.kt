package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardPaymentConfig(
    val integrationType: IntegrationType = DEFAULT_INTEGRATION_TYPE,
    val ipAddress: String = DEFAULT_IP,
    val port: Int = DEFAULT_PORT
) : Parcelable {
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
