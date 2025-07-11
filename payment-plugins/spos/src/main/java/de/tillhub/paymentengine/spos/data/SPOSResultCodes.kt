package de.tillhub.paymentengine.spos.data

import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.spos.R

internal object SPOSResultCodes {
    private val resultCodeSet = mapOf(
        Pair(
            "S_SWITCH_NOT_CONNECTED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_terminal_not_connected,
                recoveryMessages = listOf(R.string.spos_error_terminal_not_connected_action)
            )
        ),
        Pair(
            "CARD_PAYMENT_NOT_ONBOARDED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_terminal_not_onboarded,
                recoveryMessages = listOf(R.string.spos_error_terminal_not_onboarded_action)
            )
        ),
        Pair(
            "AMOUNT_REQUIRED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_amount_required,
                recoveryMessages = listOf(R.string.spos_error_amount_required_action)
            )
        ),
        Pair(
            "CARD_DETECTION_FAILED",
            TransactionResultCode.Known(R.string.spos_error_card_detection_failed)
        ),
        Pair(
            "CARD_READ_FAILED",
            TransactionResultCode.Known(R.string.spos_error_card_read_failed)
        ),
        Pair(
            "CARD_READER_STATUS_FAILED",
            TransactionResultCode.Known(R.string.spos_error_card_reader_status_failed)
        ),
        Pair(
            "FLOW_ALREADY_HAPPENING",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_flow_already_happening,
                recoveryMessages = listOf(R.string.spos_error_flow_already_happening_action)
            )
        ),
        Pair(
            "INVALID_REQUEST_TYPE",
            TransactionResultCode.Known(R.string.spos_error_invalid_request_type)
        ),
        Pair(
            "RESPONSE_TIMEOUT",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_response_timeout,
                recoveryMessages = listOf(R.string.spos_error_response_timeout_action)
            )
        ),
        Pair(
            "STATUS_EMAIL_VERIFICATION_TIMEOUT_SERVER",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_email_verification_timeout_server,
                recoveryMessages = listOf(R.string.spos_error_email_verification_timeout_server_action)
            )
        ),
        Pair(
            "STATUS_NOT_INSTALLED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_not_installed,
                recoveryMessages = listOf(R.string.spos_error_not_installed_action)
            )
        ),
        Pair(
            "STATUS_PERSONALIZATION_NOT_DONE_YET",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_personalization_not_done,
                recoveryMessages = listOf(R.string.spos_error_personalization_not_done_action)
            )
        ),
        Pair(
            "STATUS_TERMINAL_NOT_OPERATIONAL",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_terminal_not_operational,
                recoveryMessages = listOf(R.string.spos_error_terminal_not_operational_action)
            )
        ),
        Pair(
            "STATUS_WBC_KEYS_EXPIRED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_wbc_keys_expired,
                recoveryMessages = listOf(R.string.spos_error_wbc_keys_expired_action)
            )
        ),
        Pair(
            "TERMINAL_CONNECTION_LOST",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_terminal_connection_lost,
                recoveryMessages = listOf(R.string.spos_error_terminal_connection_lost_action)
            )
        ),
        Pair(
            "STATUS_PERSONALIZATION_FAILED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_personalization_failed,
                recoveryMessages = listOf(R.string.spos_error_personalization_failed_action)
            )
        ),
        Pair(
            "STATUS_THREAT_DETECTION_TRIGGERED",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_threat_detection_triggered,
                recoveryMessages = listOf(R.string.spos_error_threat_detection_triggered_action)
            )
        ),
        Pair(
            "STATUS_GENERAL_ERROR",
            TransactionResultCode.Known(R.string.spos_error_general_error)
        ),
        Pair(
            "PAYMENT_ERROR",
            TransactionResultCode.Known(R.string.spos_error_payment_error)
        ),
        Pair(
            "REPEAT_LAST_MESSAGE_ERROR",
            TransactionResultCode.Known(R.string.spos_error_repeat_last_message_error)
        ),
        Pair(
            "APP_NOT_IN_FOREGROUND",
            TransactionResultCode.Known(
                errorMessage = R.string.spos_error_app_not_in_foreground,
                recoveryMessages = listOf(R.string.spos_error_app_not_in_foreground_action)
            )
        ),
    )

    fun getSPOSCode(resultCode: String?): TransactionResultCode {
        return resultCodeSet.getOrDefault(
            key = resultCode,
            defaultValue = TransactionResultCode.Unknown(
                resultCode = UNKNOWN_RESULT_CODE,
                resultCodeString = resultCode,
                errorMessage = de.tillhub.paymentengine.R.string.error_code_unknown
            )
        )
    }

    private const val UNKNOWN_RESULT_CODE = -1
    val APP_NOT_FOUND_ERROR = TransactionResultCode.Known(R.string.spos_error_app_not_found)
}