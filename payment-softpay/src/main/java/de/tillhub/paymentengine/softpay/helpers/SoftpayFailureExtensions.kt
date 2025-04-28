package de.tillhub.paymentengine.softpay.helpers

import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.softpay.R
import io.softpay.sdk.failure.Failure

internal fun Failure.toTransactionResultCode() : TransactionResultCode {
    return TransactionResultCode.Unknown(
        resultCode = this.code,
        errorMessage = R.string.softpay_error_general,
        recoveryMessages = listOf(R.string.softpay_recovery_contact_support)
    )
}