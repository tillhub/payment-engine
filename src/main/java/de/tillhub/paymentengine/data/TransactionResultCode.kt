import androidx.annotation.StringRes
import de.tillhub.paymentengine.R

data class TransactionResultCode(
    @StringRes
    val errorMessage: Int,
    @StringRes
    val recoveryMessages: List<Int> = listOf(R.string.recovery_message_service_technician_fix)
)

@SuppressWarnings("MagicNumber")
sealed class ResultCodeSets constructor(
    val mapping: Map<Int, TransactionResultCode>
) {
    object CommonResultCodes : ResultCodeSets(mapOf(
        Pair(156, TransactionResultCode(
            errorMessage = R.string.common_result_code_156_please_wait,
            recoveryMessages = emptyList()
        )),
        Pair(177, TransactionResultCode(
            errorMessage = R.string.common_result_code_177_memory_full,
            recoveryMessages = listOf(
                R.string.recovery_message_end_of_day,
                R.string.recovery_message_service_technician_fix
            )
        )),
        Pair(178, TransactionResultCode(
            errorMessage = R.string.common_result_code_178_journal_full,
            recoveryMessages = listOf(
                R.string.recovery_message_read_file,
                R.string.recovery_message_delete_file
            )
        )),
        Pair(191, TransactionResultCode(
            errorMessage = R.string.common_result_code_191_voltage_low
        )),
        Pair(192, TransactionResultCode(errorMessage = R.string.common_result_code_192_mechanism_defect)),
        Pair(193, TransactionResultCode(
            errorMessage = R.string.common_result_code_193_merchant_card_locked,
            recoveryMessages = listOf(
                R.string.recovery_message_set_date_time,
                R.string.recovery_message_diagnosis,
                R.string.recovery_message_service_technician_fix
            )
        )),
        Pair(194, TransactionResultCode(
            errorMessage = R.string.common_result_code_194_diagnosis_required,
            recoveryMessages = listOf(R.string.recovery_message_diagnosis)
        )),
        Pair(204, TransactionResultCode(
            errorMessage = R.string.common_result_code_204_printer_not_ready
        )),
        Pair(220, TransactionResultCode(
            errorMessage = R.string.common_result_code_220_card_inserted,
            recoveryMessages = listOf(R.string.recovery_message_proceed_or_abort)
        )),
        Pair(223, TransactionResultCode(errorMessage = R.string.common_result_code_223_out_of_order)),
        Pair(224, TransactionResultCode(
            errorMessage = R.string.common_result_code_224_remote_maintenance
        )),
        Pair(225, TransactionResultCode(
            errorMessage = R.string.common_result_code_225_card_not_removed,
            recoveryMessages = listOf(R.string.recovery_message_extract_card)
        )),
        Pair(226, TransactionResultCode(
            errorMessage = R.string.common_result_code_226_card_reader_defective
        )),
        Pair(227, TransactionResultCode(errorMessage = R.string.common_result_code_227_shutter_closed)),
        Pair(228, TransactionResultCode(
            errorMessage = R.string.common_result_code_228_terminal_activation_required
        )),
        Pair(240, TransactionResultCode(errorMessage = R.string.common_result_code_240_reconciliation_required)),
        Pair(246, TransactionResultCode(
            errorMessage = R.string.common_result_code_246_opt_not_available
        )),
    ))

    sealed class ProviderSpecificResultCodes constructor(
        mapping: Map<Int, TransactionResultCode>
    ) : ResultCodeSets(mapping) {
        object LavegoResultCodes : ProviderSpecificResultCodes(mapOf(
            Pair(2, TransactionResultCode(R.string.lavego_result_code_2_call_merchant)),
            Pair(3, TransactionResultCode(R.string.lavego_result_code_3_invalid_merchant_number)),
            Pair(4, TransactionResultCode(R.string.lavego_result_code_4_card_not_admitted)),
            Pair(5, TransactionResultCode(R.string.lavego_result_code_5_declined)),
            Pair(9, TransactionResultCode(R.string.lavego_result_code_9_request_in_progress)),
            Pair(10, TransactionResultCode(R.string.lavego_result_code_10_partially_approved)),
            Pair(12, TransactionResultCode(R.string.lavego_result_code_12_invalid_transaction)),
            Pair(13, TransactionResultCode(R.string.lavego_result_code_13_invalid_amount)),
            Pair(14, TransactionResultCode(R.string.lavego_result_code_14_invalid_card_number)),
            Pair(19, TransactionResultCode(R.string.lavego_result_code_19_tx_count_exceeded)),
            Pair(21, TransactionResultCode(R.string.lavego_result_code_21_not_accepted)),
            Pair(30, TransactionResultCode(R.string.lavego_result_code_30_system_malfunction)),
            Pair(33, TransactionResultCode(R.string.lavego_result_code_33_expired_card)),
            Pair(34, TransactionResultCode(R.string.lavego_result_code_34_invalid_function)),
            Pair(40, TransactionResultCode(R.string.lavego_result_code_40_pick_up_card)),
            Pair(43, TransactionResultCode(R.string.lavego_result_code_43_tx_not_permitted)),
            Pair(48, TransactionResultCode(R.string.lavego_result_code_48_pin_not_active)),
            Pair(51, TransactionResultCode(R.string.lavego_result_code_51_not_accepted)),
            Pair(54, TransactionResultCode(R.string.lavego_result_code_54_expired_card)),
            Pair(55, TransactionResultCode(R.string.lavego_result_code_55_incorrect_pin)),
            Pair(56, TransactionResultCode(R.string.lavego_result_code_56_invalid_card)),
            Pair(57, TransactionResultCode(R.string.lavego_result_code_57_wrong_card)),
            Pair(58, TransactionResultCode(R.string.lavego_result_code_58_terminal_not_permitted)),
            Pair(59, TransactionResultCode(R.string.lavego_result_code_59_not_accepted)),
            Pair(60, TransactionResultCode(R.string.lavego_result_code_60_retry_contact)),
            Pair(62, TransactionResultCode(R.string.lavego_result_code_62_restricted_card)),
            Pair(64, TransactionResultCode(R.string.lavego_result_code_64_original_amount_incorrect)),
            Pair(65, TransactionResultCode(R.string.lavego_result_code_65_no_pin_limit_exceeded)),
            Pair(75, TransactionResultCode(R.string.lavego_result_code_75_pin_try_limit_exceeded)),
            Pair(80, TransactionResultCode(R.string.lavego_result_code_80_referenced_tx_unknown)),
            Pair(81, TransactionResultCode(
                errorMessage = R.string.lavego_result_code_81_initialisation_required,
                recoveryMessages = listOf(R.string.recovery_message_initialisation)
            )),
            Pair(84, TransactionResultCode(R.string.lavego_result_code_84_pin_not_active)),
            Pair(85, TransactionResultCode(R.string.lavego_result_code_85_cashback_not_possible)),
            Pair(91, TransactionResultCode(R.string.lavego_result_code_91_card_issuer_inoperative)),
            Pair(92, TransactionResultCode(R.string.lavego_result_code_92_invalid_card_type)),
            Pair(96, TransactionResultCode(R.string.lavego_result_code_96_system_inoperative)),
            Pair(98, TransactionResultCode(
                errorMessage = R.string.lavego_result_code_98_invalid_date_time,
                recoveryMessages = listOf(
                    R.string.recovery_message_set_date_time,
                    R.string.recovery_message_diagnosis,
                )
            )),
        ))
    }
}
