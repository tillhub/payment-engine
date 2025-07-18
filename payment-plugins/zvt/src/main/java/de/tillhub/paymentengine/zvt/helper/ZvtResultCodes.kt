package de.tillhub.paymentengine.zvt.helper

import de.tillhub.paymentengine.data.TransactionResultCode
import de.tillhub.paymentengine.zvt.R

@SuppressWarnings("MagicNumber")
internal object ZvtResultCodes {
    private const val UNKNOWN_RESULT_CODE = -1

    val zvtMapping = mapOf(
        Pair(
            0,
            TransactionResultCode.Known(
                R.string.zvt_error_code_00,
                listOf(R.string.recovery_message_no_action)
            )
        ),
        Pair(
            81,
            TransactionResultCode.Known(
                R.string.zvt_error_code_81,
                listOf(R.string.recovery_message_initialisation)
            )
        ),
        Pair(
            98,
            TransactionResultCode.Known(
                R.string.zvt_error_code_98,
                listOf(
                    R.string.recovery_message_set_date_time,
                    R.string.recovery_message_diagnosis
                )
            )
        ),
        Pair(
            100,
            TransactionResultCode.Known(
                R.string.zvt_error_code_100,
                listOf(R.string.recovery_message_repeat_card_insertion)
            )
        ),
        Pair(
            101,
            TransactionResultCode.Known(
                R.string.zvt_error_code_101,
                listOf(R.string.recovery_message_start_otp)
            )
        ),
        Pair(102, TransactionResultCode.Known(R.string.zvt_error_code_102)),
        Pair(103, TransactionResultCode.Known(R.string.zvt_error_code_103)),
        Pair(104, TransactionResultCode.Known(R.string.zvt_error_code_104)),
        Pair(106, TransactionResultCode.Known(R.string.zvt_error_code_106)),
        Pair(107, TransactionResultCode.Known(R.string.zvt_error_code_107)),
        Pair(108, TransactionResultCode.Known(R.string.zvt_error_code_108)),
        Pair(110, TransactionResultCode.Known(R.string.zvt_error_code_110)),
        Pair(111, TransactionResultCode.Known(R.string.zvt_error_code_111)),
        Pair(113, TransactionResultCode.Known(R.string.zvt_error_code_113)),
        Pair(114, TransactionResultCode.Known(R.string.zvt_error_code_114)),
        Pair(115, TransactionResultCode.Known(R.string.zvt_error_code_115)),
        Pair(119, TransactionResultCode.Known(R.string.zvt_error_code_119)),
        Pair(120, TransactionResultCode.Known(R.string.zvt_error_code_120)),
        Pair(121, TransactionResultCode.Known(R.string.zvt_error_code_121)),
        Pair(122, TransactionResultCode.Known(R.string.zvt_error_code_122)),
        Pair(123, TransactionResultCode.Known(R.string.zvt_error_code_123)),
        Pair(124, TransactionResultCode.Known(R.string.zvt_error_code_124)),
        Pair(125, TransactionResultCode.Known(R.string.zvt_error_code_125)),
        Pair(126, TransactionResultCode.Known(R.string.zvt_error_code_126)),
        Pair(131, TransactionResultCode.Known(R.string.zvt_error_code_131)),
        Pair(133, TransactionResultCode.Known(R.string.zvt_error_code_133)),
        Pair(137, TransactionResultCode.Known(R.string.zvt_error_code_137)),
        Pair(154, TransactionResultCode.Known(R.string.zvt_error_code_154)),
        Pair(155, TransactionResultCode.Known(R.string.zvt_error_code_155)),
        Pair(
            156,
            TransactionResultCode.Known(
                R.string.zvt_error_code_156,
                listOf(R.string.recovery_message_wait, R.string.recovery_message_no_action)
            )
        ),
        Pair(157, TransactionResultCode.Known(R.string.zvt_error_code_157)),
        Pair(160, TransactionResultCode.Known(R.string.zvt_error_code_160)),
        Pair(161, TransactionResultCode.Known(R.string.zvt_error_code_161)),
        Pair(163, TransactionResultCode.Known(R.string.zvt_error_code_163)),
        Pair(164, TransactionResultCode.Known(R.string.zvt_error_code_164)),
        Pair(
            177,
            TransactionResultCode.Known(
                R.string.zvt_error_code_177,
                listOf(
                    R.string.recovery_message_end_of_day,
                    R.string.recovery_message_service_technician_fix
                )
            )
        ),
        Pair(
            178,
            TransactionResultCode.Known(
                R.string.zvt_error_code_178,
                listOf(
                    R.string.recovery_message_read_file,
                    R.string.recovery_message_delete_file
                )
            )
        ),
        Pair(180, TransactionResultCode.Known(R.string.zvt_error_code_180)),
        Pair(181, TransactionResultCode.Known(R.string.zvt_error_code_181)),
        Pair(183, TransactionResultCode.Known(R.string.zvt_error_code_183)),
        Pair(184, TransactionResultCode.Known(R.string.zvt_error_code_184)),
        Pair(
            191,
            TransactionResultCode.Known(
                R.string.zvt_error_code_191,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(
            192,
            TransactionResultCode.Known(
                R.string.zvt_error_code_192,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(
            193,
            TransactionResultCode.Known(
                R.string.zvt_error_code_193,
                listOf(
                    R.string.recovery_message_set_date_time,
                    R.string.recovery_message_diagnosis,
                    R.string.recovery_message_service_technician_fix
                )
            )
        ),
        Pair(
            194,
            TransactionResultCode.Known(
                R.string.zvt_error_code_194,
                listOf(R.string.recovery_message_diagnosis)
            )
        ),
        Pair(195, TransactionResultCode.Known(R.string.zvt_error_code_195)),
        Pair(
            196,
            TransactionResultCode.Known(
                R.string.zvt_error_code_196,
                listOf(
                    R.string.recovery_message_software_update,
                    R.string.recovery_message_service_technician_fix
                )
            )
        ),
        Pair(197, TransactionResultCode.Known(R.string.zvt_error_code_197)),
        Pair(198, TransactionResultCode.Known(R.string.zvt_error_code_198)),
        Pair(200, TransactionResultCode.Known(R.string.zvt_error_code_200)),
        Pair(201, TransactionResultCode.Known(R.string.zvt_error_code_201)),
        Pair(203, TransactionResultCode.Known(R.string.zvt_error_code_203)),
        Pair(
            204,
            TransactionResultCode.Known(
                R.string.zvt_error_code_204,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(205, TransactionResultCode.Known(R.string.zvt_error_code_205)),
        Pair(210, TransactionResultCode.Known(R.string.zvt_error_code_210)),
        Pair(
            220,
            TransactionResultCode.Known(
                R.string.zvt_error_code_220,
                listOf(R.string.recovery_message_proceed_or_abort)
            )
        ),
        Pair(221, TransactionResultCode.Known(R.string.zvt_error_code_221)),
        Pair(222, TransactionResultCode.Known(R.string.zvt_error_code_222)),
        Pair(
            223,
            TransactionResultCode.Known(
                R.string.zvt_error_code_223,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(
            224,
            TransactionResultCode.Known(
                R.string.zvt_error_code_224,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(
            225,
            TransactionResultCode.Known(
                R.string.zvt_error_code_225,
                listOf(R.string.recovery_message_extract_card)
            )
        ),
        Pair(
            226,
            TransactionResultCode.Known(
                R.string.zvt_error_code_226,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(227, TransactionResultCode.Known(R.string.zvt_error_code_227)),
        Pair(
            228,
            TransactionResultCode.Known(
                R.string.zvt_error_code_228,
                listOf(R.string.recovery_message_service_technician_fix)
            )
        ),
        Pair(231, TransactionResultCode.Known(R.string.zvt_error_code_231)),
        Pair(232, TransactionResultCode.Known(R.string.zvt_error_code_232)),
        Pair(233, TransactionResultCode.Known(R.string.zvt_error_code_233)),
        Pair(234, TransactionResultCode.Known(R.string.zvt_error_code_234)),
        Pair(235, TransactionResultCode.Known(R.string.zvt_error_code_235)),
        Pair(236, TransactionResultCode.Known(R.string.zvt_error_code_236)),
        Pair(237, TransactionResultCode.Known(R.string.zvt_error_code_237)),
        Pair(240, TransactionResultCode.Known(R.string.zvt_error_code_240)),
        Pair(241, TransactionResultCode.Known(R.string.zvt_error_code_241)),
        Pair(245, TransactionResultCode.Known(R.string.zvt_error_code_245)),
        Pair(246, TransactionResultCode.Known(R.string.zvt_error_code_246)),
        Pair(250, TransactionResultCode.Known(R.string.zvt_error_code_250)),
        Pair(251, TransactionResultCode.Known(R.string.zvt_error_code_251)),
        Pair(252, TransactionResultCode.Known(R.string.zvt_error_code_252)),
        Pair(253, TransactionResultCode.Known(R.string.zvt_error_code_253)),
        Pair(254, TransactionResultCode.Known(R.string.zvt_error_code_254)),
        Pair(255, TransactionResultCode.Known(R.string.zvt_error_code_255))
    )
    val lavegoMapping = mapOf(
        Pair(2, TransactionResultCode.Known(R.string.lavego_result_code_2_call_merchant)),
        Pair(
            3,
            TransactionResultCode.Known(
                R.string.lavego_result_code_3_invalid_merchant_number
            )
        ),
        Pair(4, TransactionResultCode.Known(R.string.lavego_result_code_4_card_not_admitted)),
        Pair(5, TransactionResultCode.Known(R.string.lavego_result_code_5_declined)),
        Pair(9, TransactionResultCode.Known(R.string.lavego_result_code_9_request_in_progress)),
        Pair(
            10,
            TransactionResultCode.Known(
                R.string.lavego_result_code_10_partially_approved
            )
        ),
        Pair(
            12,
            TransactionResultCode.Known(
                R.string.lavego_result_code_12_invalid_transaction
            )
        ),
        Pair(13, TransactionResultCode.Known(R.string.lavego_result_code_13_invalid_amount)),
        Pair(
            14,
            TransactionResultCode.Known(
                R.string.lavego_result_code_14_invalid_card_number
            )
        ),
        Pair(19, TransactionResultCode.Known(R.string.lavego_result_code_19_tx_count_exceeded)),
        Pair(21, TransactionResultCode.Known(R.string.lavego_result_code_21_not_accepted)),
        Pair(
            30,
            TransactionResultCode.Known(
                R.string.lavego_result_code_30_system_malfunction
            )
        ),
        Pair(33, TransactionResultCode.Known(R.string.lavego_result_code_33_expired_card)),
        Pair(34, TransactionResultCode.Known(R.string.lavego_result_code_34_invalid_function)),
        Pair(40, TransactionResultCode.Known(R.string.lavego_result_code_40_pick_up_card)),
        Pair(43, TransactionResultCode.Known(R.string.lavego_result_code_43_tx_not_permitted)),
        Pair(48, TransactionResultCode.Known(R.string.lavego_result_code_48_pin_not_active)),
        Pair(51, TransactionResultCode.Known(R.string.lavego_result_code_51_not_accepted)),
        Pair(54, TransactionResultCode.Known(R.string.lavego_result_code_54_expired_card)),
        Pair(55, TransactionResultCode.Known(R.string.lavego_result_code_55_incorrect_pin)),
        Pair(56, TransactionResultCode.Known(R.string.lavego_result_code_56_invalid_card)),
        Pair(57, TransactionResultCode.Known(R.string.lavego_result_code_57_wrong_card)),
        Pair(
            58,
            TransactionResultCode.Known(
                R.string.lavego_result_code_58_terminal_not_permitted
            )
        ),
        Pair(59, TransactionResultCode.Known(R.string.lavego_result_code_59_not_accepted)),
        Pair(60, TransactionResultCode.Known(R.string.lavego_result_code_60_retry_contact)),
        Pair(62, TransactionResultCode.Known(R.string.lavego_result_code_62_restricted_card)),
        Pair(
            64,
            TransactionResultCode.Known(
                R.string.lavego_result_code_64_original_amount_incorrect
            )
        ),
        Pair(
            65,
            TransactionResultCode.Known(
                R.string.lavego_result_code_65_no_pin_limit_exceeded
            )
        ),
        Pair(
            75,
            TransactionResultCode.Known(
                R.string.lavego_result_code_75_pin_try_limit_exceeded
            )
        ),
        Pair(
            80,
            TransactionResultCode.Known(
                R.string.lavego_result_code_80_referenced_tx_unknown
            )
        ),
        Pair(
            81,
            TransactionResultCode.Known(
                errorMessage = R.string.lavego_result_code_81_initialisation_required,
                recoveryMessages = listOf(R.string.recovery_message_initialisation)
            )
        ),
        Pair(84, TransactionResultCode.Known(R.string.lavego_result_code_84_pin_not_active)),
        Pair(
            85,
            TransactionResultCode.Known(
                R.string.lavego_result_code_85_cashback_not_possible
            )
        ),
        Pair(
            91,
            TransactionResultCode.Known(
                R.string.lavego_result_code_91_card_issuer_inoperative
            )
        ),
        Pair(92, TransactionResultCode.Known(R.string.lavego_result_code_92_invalid_card_type)),
        Pair(96, TransactionResultCode.Known(R.string.lavego_result_code_96_system_inoperative))
    )

    fun getZVTCode(resultCode: Int?): TransactionResultCode {
        return lavegoMapping.getOrDefault(
            key = resultCode,
            defaultValue = zvtMapping.getOrDefault(
                key = resultCode,
                defaultValue = TransactionResultCode.Unknown(
                    resultCode = resultCode ?: UNKNOWN_RESULT_CODE,
                    errorMessage = R.string.zvt_error_code_unknown
                )
            )
        )
    }
}