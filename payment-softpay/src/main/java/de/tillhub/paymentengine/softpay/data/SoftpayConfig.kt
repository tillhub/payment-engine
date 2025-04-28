package de.tillhub.paymentengine.softpay.data

import java.util.Objects

class SoftpayConfig(
    val integratorId: String,
    val accessId: String,
    val accessSecret: String,
    val merchantUsername: String,
    val merchantPassword: String
) {
    override fun toString() = "SoftpayConfig(" +
            "integratorId=$integratorId, " +
            "accessId=$accessId, " +
            "accessSecret=$accessSecret, " +
            "merchantUsername=$merchantUsername, " +
            "merchantPassword=$merchantPassword" +
            ")"

    override fun equals(other: Any?) = other is SoftpayConfig &&
            integratorId == other.integratorId &&
            accessId == other.accessId &&
            accessSecret == other.accessSecret &&
            merchantUsername == other.merchantUsername &&
            merchantPassword == other.merchantPassword

    override fun hashCode() = Objects.hash(
        integratorId,
        accessId,
        accessSecret,
        merchantUsername,
        merchantPassword
    )
}