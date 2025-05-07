package de.tillhub.paymentengine.softpay.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class SoftpayConfig(
    val merchantUsername: String,
    val merchantPassword: String,
    val storeId: String,
): Parcelable {
    override fun toString() = "SoftpayConfig(" +
            "merchantUsername=$merchantUsername, " +
            "merchantPassword=$merchantPassword," +
            "storeId=$storeId," +
            ")"

    override fun equals(other: Any?) = other is SoftpayConfig &&
            storeId == other.storeId &&
            merchantUsername == other.merchantUsername &&
            merchantPassword == other.merchantPassword

    override fun hashCode() = Objects.hash(
        storeId,
        merchantUsername,
        merchantPassword
    )
}