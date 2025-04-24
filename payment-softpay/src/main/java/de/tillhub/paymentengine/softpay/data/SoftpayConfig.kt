package de.tillhub.paymentengine.softpay.data

import java.util.Objects

class SoftpayConfig(
    val accessId: String,
    val accessSecret: String,
    val merchantUsername: String,
    val merchantPassword: String
) {
    override fun toString() = "SoftpayConfig(" +
            "accessId=$accessId, " +
            "accessSecret=$accessSecret, " +
            "merchantUsername=$merchantUsername, " +
            "merchantPassword=$merchantPassword" +
            ")"

    override fun equals(other: Any?) = other is SoftpayConfig &&
            accessId == other.accessId &&
            accessSecret == other.accessSecret &&
            merchantUsername == other.merchantUsername &&
            merchantPassword == other.merchantPassword

    override fun hashCode() = Objects.hash(
        accessId,
        accessSecret,
        merchantUsername,
        merchantPassword
    )
}