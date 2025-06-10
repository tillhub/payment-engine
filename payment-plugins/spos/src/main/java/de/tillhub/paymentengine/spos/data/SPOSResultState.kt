package de.tillhub.paymentengine.spos.data

internal enum class SPOSResultState(val value: String) {
    SUCCESS("Success"),
    FAILURE("Failure"),
    ABORTED("Aborted"),
    BUSY("Busy"),
    COMMUNICATION_ERROR("CommunicationError"),
    DEVICE_CONFIGURATION_FAILURE("DeviceConfigurationFailure"),
    DEVICE_UNAVAILABLE("DeviceUnavailable"),
    FORMAT_ERROR("FormatError"),
    MISSING_MANDATORY_DATA("MissingMandatoryData"),
    NO_ACTIVE_PAYMENT("NoActivePayment"),
    PARSING_ERROR("ParsingError"),
    PARTIAL_FAILURE("PartialFailure"),
    PAYMENT_ONGOING("PaymentOnGoing"),
    PC_COMMUNICATION_FAILED("PcCommunicationFailed"),
    DEVICE_CONFIGURATION_FAILED("DeviceConfigurationFailed"),
    PRINT_LAST_TICKET("PrintLastTicket"),
    TIMED_OUT("TimedOut"),
    RECEIPT_CALL_FAILED("ReceiptCallFailed"),
    TERMINAL_ALREADY_ACTIVATED("TerminalAlreadyActivated"),
    VALIDATION_ERROR("ValidationError"),
    TERMINAL_UNKNOWN("Unknown"),
    UNKNOWN("unknown");

    companion object {
        fun find(type: String): SPOSResultState =
            SPOSResultState.entries.find { it.value == type } ?: UNKNOWN
    }
}