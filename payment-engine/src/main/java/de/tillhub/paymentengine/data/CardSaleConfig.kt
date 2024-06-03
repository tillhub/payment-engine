package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class CardSaleConfig(
    val applicationName: String = DEFAULT_APPLICATION_NAME,
    val operatorId: String = DEFAULT_OPERATOR_ID,
    val saleId: String = DEFAULT_SALE_ID, // Unique cash register ID
    val pin: String = DEFAULT_PIN, // Terminal pin
    val poiId: String = DEFAULT_POI_ID, // Unique terminal ID
    val poiSerialNumber: String = DEFAULT_POI_SERIAL, // Unique terminal serial
) : Parcelable, Comparable<CardSaleConfig> {

    override fun toString() = "CardSaleConfig(" +
            "applicationName=$applicationName, " +
            "operatorId=$operatorId, " +
            "saleId=$saleId, " +
            "pin=$pin, " +
            "poiId=$poiId, " +
            "poiSerialNumber=$poiSerialNumber" +
            ")"

    override fun compareTo(other: CardSaleConfig): Int =
        applicationName.compareTo(other.applicationName) +
        operatorId.compareTo(other.operatorId) +
        saleId.compareTo(other.saleId) +
        pin.compareTo(other.pin) +
        poiId.compareTo(other.poiId) +
        poiSerialNumber.compareTo(other.poiSerialNumber)

    override fun equals(other: Any?) = other is CardSaleConfig &&
            applicationName == other.applicationName &&
            operatorId == other.operatorId &&
            saleId == other.saleId &&
            pin == other.pin &&
            poiId == other.poiId &&
            poiSerialNumber == other.poiSerialNumber

    override fun hashCode() = Objects.hash(
        applicationName,
        operatorId,
        saleId,
        pin,
        poiId,
        poiSerialNumber
    )

    companion object {
        private const val DEFAULT_APPLICATION_NAME = "Tillhub GO"
        private const val DEFAULT_OPERATOR_ID = "ah"
        const val DEFAULT_SALE_ID = "registerProvider" // Unique cash register ID
        const val DEFAULT_PIN = "333333" // CCV Fly 2222
        const val DEFAULT_POI_ID = "66000001" // Unique terminal ID
        const val DEFAULT_POI_SERIAL = "" // Unique terminal serial
    }
}