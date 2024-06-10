package de.tillhub.paymentengine.data

import android.os.Parcelable
import androidx.annotation.StringRes
import de.tillhub.paymentengine.R
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class TransactionResultCode(
    @StringRes
    val errorMessage: Int,
    @StringRes
    val recoveryMessages: List<Int> = listOf()
) : Parcelable {
    override fun equals(other: Any?) = other is TransactionResultCode &&
            errorMessage == other.errorMessage &&
            recoveryMessages == other.recoveryMessages

    override fun hashCode() = Objects.hash(
        errorMessage,
        recoveryMessages
    )
    override fun toString() = "TransactionResultCode(" +
            "errorMessage=$errorMessage, " +
            "recoveryMessages=$recoveryMessages" +
            ")"
}

@SuppressWarnings("MagicNumber")
internal sealed class ResultCodeSets(val mapping: Map<Int, TransactionResultCode>) {
    data object OpiResultCodes : ResultCodeSets(
        mapOf(
            Pair(0, TransactionResultCode(R.string.opi_error_code_00)),
            Pair(1, TransactionResultCode(R.string.opi_error_code_01)),
            Pair(2, TransactionResultCode(R.string.opi_error_code_02)),
            Pair(3, TransactionResultCode(R.string.opi_error_code_03)),
            Pair(4, TransactionResultCode(R.string.opi_error_code_04)),
            Pair(5, TransactionResultCode(R.string.opi_error_code_05)),
            Pair(6, TransactionResultCode(R.string.opi_error_code_06)),
            Pair(7, TransactionResultCode(R.string.opi_error_code_07)),
            Pair(8, TransactionResultCode(R.string.opi_error_code_08)),
            Pair(9, TransactionResultCode(R.string.opi_error_code_09)),
            Pair(10, TransactionResultCode(R.string.opi_error_code_10)),
            Pair(11, TransactionResultCode(R.string.opi_error_code_11)),
            Pair(12, TransactionResultCode(R.string.opi_error_code_12)),
            Pair(13, TransactionResultCode(R.string.opi_error_code_13)),
            Pair(14, TransactionResultCode(R.string.opi_error_code_14)),
            Pair(15, TransactionResultCode(R.string.opi_error_code_15)),
            Pair(16, TransactionResultCode(R.string.opi_error_code_16)),
            Pair(17, TransactionResultCode(R.string.opi_error_code_17)),
            Pair(18, TransactionResultCode(R.string.opi_error_code_18)),
            Pair(19, TransactionResultCode(R.string.opi_error_code_19)),
            Pair(20, TransactionResultCode(R.string.opi_error_code_20)),
            Pair(21, TransactionResultCode(R.string.opi_error_code_21)),
            Pair(22, TransactionResultCode(R.string.opi_error_code_22)),
            Pair(23, TransactionResultCode(R.string.opi_error_code_23)),
            Pair(24, TransactionResultCode(R.string.opi_error_code_24)),
            Pair(25, TransactionResultCode(R.string.opi_error_code_25)),
            Pair(26, TransactionResultCode(R.string.opi_error_code_26)),
            Pair(27, TransactionResultCode(R.string.opi_error_code_27)),
            Pair(28, TransactionResultCode(R.string.opi_error_code_28)),
            Pair(29, TransactionResultCode(R.string.opi_error_code_29)),
            Pair(30, TransactionResultCode(R.string.opi_error_code_30)),
            Pair(31, TransactionResultCode(R.string.opi_error_code_31)),
            Pair(32, TransactionResultCode(R.string.opi_error_code_32)),
            Pair(33, TransactionResultCode(R.string.opi_error_code_33)),
            Pair(34, TransactionResultCode(R.string.opi_error_code_34)),
            Pair(35, TransactionResultCode(R.string.opi_error_code_35)),
            Pair(36, TransactionResultCode(R.string.opi_error_code_36)),
            Pair(37, TransactionResultCode(R.string.opi_error_code_37)),
            Pair(38, TransactionResultCode(R.string.opi_error_code_38)),
            Pair(39, TransactionResultCode(R.string.opi_error_code_39)),
            Pair(40, TransactionResultCode(R.string.opi_error_code_40)),
            Pair(41, TransactionResultCode(R.string.opi_error_code_41)),
            Pair(42, TransactionResultCode(R.string.opi_error_code_42)),
            Pair(43, TransactionResultCode(R.string.opi_error_code_43)),
            Pair(44, TransactionResultCode(R.string.opi_error_code_44)),
            Pair(51, TransactionResultCode(R.string.opi_error_code_51)),
            Pair(52, TransactionResultCode(R.string.opi_error_code_52)),
            Pair(53, TransactionResultCode(R.string.opi_error_code_53)),
            Pair(54, TransactionResultCode(R.string.opi_error_code_54)),
            Pair(55, TransactionResultCode(R.string.opi_error_code_55)),
            Pair(56, TransactionResultCode(R.string.opi_error_code_56)),
            Pair(57, TransactionResultCode(R.string.opi_error_code_57)),
            Pair(58, TransactionResultCode(R.string.opi_error_code_58)),
            Pair(59, TransactionResultCode(R.string.opi_error_code_59)),
            Pair(60, TransactionResultCode(R.string.opi_error_code_60)),
            Pair(61, TransactionResultCode(R.string.opi_error_code_61)),
            Pair(62, TransactionResultCode(R.string.opi_error_code_62)),
            Pair(63, TransactionResultCode(R.string.opi_error_code_63)),
            Pair(64, TransactionResultCode(R.string.opi_error_code_64)),
            Pair(65, TransactionResultCode(R.string.opi_error_code_65)),
            Pair(66, TransactionResultCode(R.string.opi_error_code_66)),
            Pair(67, TransactionResultCode(R.string.opi_error_code_67)),
            Pair(68, TransactionResultCode(R.string.opi_error_code_68)),
            Pair(75, TransactionResultCode(R.string.opi_error_code_75)),
            Pair(78, TransactionResultCode(R.string.opi_error_code_78)),
            Pair(80, TransactionResultCode(R.string.opi_error_code_80)),
            Pair(82, TransactionResultCode(R.string.opi_error_code_82)),
            Pair(85, TransactionResultCode(R.string.opi_error_code_85)),
            Pair(90, TransactionResultCode(R.string.opi_error_code_90)),
            Pair(91, TransactionResultCode(R.string.opi_error_code_91)),
            Pair(92, TransactionResultCode(R.string.opi_error_code_92)),
            Pair(93, TransactionResultCode(R.string.opi_error_code_93)),
            Pair(94, TransactionResultCode(R.string.opi_error_code_94)),
            Pair(95, TransactionResultCode(R.string.opi_error_code_95)),
            Pair(96, TransactionResultCode(R.string.opi_error_code_96)),
            Pair(100, TransactionResultCode(R.string.opi_error_code_100)),
            Pair(101, TransactionResultCode(R.string.opi_error_code_101)),
            Pair(102, TransactionResultCode(R.string.opi_error_code_102)),
            Pair(103, TransactionResultCode(R.string.opi_error_code_103)),
            Pair(104, TransactionResultCode(R.string.opi_error_code_104)),
            Pair(105, TransactionResultCode(R.string.opi_error_code_105)),
            Pair(106, TransactionResultCode(R.string.opi_error_code_106)),
            Pair(107, TransactionResultCode(R.string.opi_error_code_107)),
            Pair(108, TransactionResultCode(R.string.opi_error_code_108)),
            Pair(109, TransactionResultCode(R.string.opi_error_code_109)),
            Pair(110, TransactionResultCode(R.string.opi_error_code_110)),
            Pair(111, TransactionResultCode(R.string.opi_error_code_111)),
            Pair(112, TransactionResultCode(R.string.opi_error_code_112)),
            Pair(113, TransactionResultCode(R.string.opi_error_code_113)),
            Pair(114, TransactionResultCode(R.string.opi_error_code_114)),
            Pair(115, TransactionResultCode(R.string.opi_error_code_115)),
            Pair(116, TransactionResultCode(R.string.opi_error_code_116)),
            Pair(117, TransactionResultCode(R.string.opi_error_code_117)),
            Pair(118, TransactionResultCode(R.string.opi_error_code_118)),
            Pair(119, TransactionResultCode(R.string.opi_error_code_119)),
            Pair(120, TransactionResultCode(R.string.opi_error_code_120)),
            Pair(121, TransactionResultCode(R.string.opi_error_code_121)),
            Pair(122, TransactionResultCode(R.string.opi_error_code_122)),
            Pair(123, TransactionResultCode(R.string.opi_error_code_123)),
            Pair(124, TransactionResultCode(R.string.opi_error_code_124)),
            Pair(125, TransactionResultCode(R.string.opi_error_code_125)),
            Pair(126, TransactionResultCode(R.string.opi_error_code_126)),
            Pair(127, TransactionResultCode(R.string.opi_error_code_127)),
            Pair(128, TransactionResultCode(R.string.opi_error_code_128)),
            Pair(129, TransactionResultCode(R.string.opi_error_code_129)),
            Pair(200, TransactionResultCode(R.string.opi_error_code_200)),
            Pair(201, TransactionResultCode(R.string.opi_error_code_201)),
            Pair(202, TransactionResultCode(R.string.opi_error_code_202)),
            Pair(203, TransactionResultCode(R.string.opi_error_code_203)),
            Pair(204, TransactionResultCode(R.string.opi_error_code_204)),
            Pair(205, TransactionResultCode(R.string.opi_error_code_205)),
            Pair(206, TransactionResultCode(R.string.opi_error_code_206)),
            Pair(207, TransactionResultCode(R.string.opi_error_code_207)),
            Pair(208, TransactionResultCode(R.string.opi_error_code_208)),
            Pair(209, TransactionResultCode(R.string.opi_error_code_209)),
            Pair(210, TransactionResultCode(R.string.opi_error_code_210)),
            Pair(300, TransactionResultCode(R.string.opi_error_code_300)),
            Pair(301, TransactionResultCode(R.string.opi_error_code_301)),
            Pair(302, TransactionResultCode(R.string.opi_error_code_302)),
            Pair(303, TransactionResultCode(R.string.opi_error_code_303)),
            Pair(304, TransactionResultCode(R.string.opi_error_code_304)),
            Pair(305, TransactionResultCode(R.string.opi_error_code_305)),
            Pair(306, TransactionResultCode(R.string.opi_error_code_306)),
            Pair(307, TransactionResultCode(R.string.opi_error_code_307)),
            Pair(308, TransactionResultCode(R.string.opi_error_code_308)),
            Pair(309, TransactionResultCode(R.string.opi_error_code_309)),
            Pair(400, TransactionResultCode(R.string.opi_error_code_400)),
            Pair(500, TransactionResultCode(R.string.opi_error_code_500)),
            Pair(501, TransactionResultCode(R.string.opi_error_code_501)),
            Pair(502, TransactionResultCode(R.string.opi_error_code_502)),
            Pair(503, TransactionResultCode(R.string.opi_error_code_503)),
            Pair(504, TransactionResultCode(R.string.opi_error_code_504)),
            Pair(600, TransactionResultCode(R.string.opi_error_code_600)),
            Pair(601, TransactionResultCode(R.string.opi_error_code_601)),
            Pair(602, TransactionResultCode(R.string.opi_error_code_602)),
            Pair(603, TransactionResultCode(R.string.opi_error_code_603)),
            Pair(604, TransactionResultCode(R.string.opi_error_code_604)),
            Pair(605, TransactionResultCode(R.string.opi_error_code_605)),
            Pair(606, TransactionResultCode(R.string.opi_error_code_606)),
            Pair(700, TransactionResultCode(R.string.opi_error_code_700)),
            Pair(800, TransactionResultCode(R.string.opi_error_code_800)),
            Pair(900, TransactionResultCode(R.string.opi_error_code_900)),
            Pair(901, TransactionResultCode(R.string.opi_error_code_901)),
            Pair(902, TransactionResultCode(R.string.opi_error_code_902)),
            Pair(903, TransactionResultCode(R.string.opi_error_code_903)),
            Pair(904, TransactionResultCode(R.string.opi_error_code_904)),
            Pair(905, TransactionResultCode(R.string.opi_error_code_905)),
            Pair(906, TransactionResultCode(R.string.opi_error_code_906)),
            Pair(907, TransactionResultCode(R.string.opi_error_code_907)),
            Pair(908, TransactionResultCode(R.string.opi_error_code_908)),
            Pair(909, TransactionResultCode(R.string.opi_error_code_909)),
            Pair(910, TransactionResultCode(R.string.opi_error_code_910)),
            Pair(911, TransactionResultCode(R.string.opi_error_code_911)),
            Pair(912, TransactionResultCode(R.string.opi_error_code_912)),
            Pair(913, TransactionResultCode(R.string.opi_error_code_913)),
            Pair(914, TransactionResultCode(R.string.opi_error_code_914)),
            Pair(915, TransactionResultCode(R.string.opi_error_code_915)),
            Pair(916, TransactionResultCode(R.string.opi_error_code_916)),
            Pair(917, TransactionResultCode(R.string.opi_error_code_917)),
            Pair(918, TransactionResultCode(R.string.opi_error_code_918)),
            Pair(919, TransactionResultCode(R.string.opi_error_code_919)),
            Pair(920, TransactionResultCode(R.string.opi_error_code_920)),
            Pair(921, TransactionResultCode(R.string.opi_error_code_921)),
            Pair(922, TransactionResultCode(R.string.opi_error_code_922)),
            Pair(923, TransactionResultCode(R.string.opi_error_code_923)),
            Pair(950, TransactionResultCode(R.string.opi_error_code_950)),
        )
    )

    data object ZvtResultCodes : ResultCodeSets(
        mapOf(
            Pair(
                0,
                TransactionResultCode(
                    R.string.zvt_error_code_00,
                    listOf(R.string.recovery_message_no_action)
                )
            ),
            Pair(
                81,
                TransactionResultCode(
                    R.string.zvt_error_code_81,
                    listOf(R.string.recovery_message_initialisation)
                )
            ),
            Pair(
                98,
                TransactionResultCode(
                    R.string.zvt_error_code_98,
                    listOf(
                        R.string.recovery_message_set_date_time,
                        R.string.recovery_message_diagnosis
                    )
                )
            ),
            Pair(
                100,
                TransactionResultCode(
                    R.string.zvt_error_code_100,
                    listOf(R.string.recovery_message_repeat_card_insertion)
                )
            ),
            Pair(
                101,
                TransactionResultCode(
                    R.string.zvt_error_code_101,
                    listOf(R.string.recovery_message_start_otp)
                )
            ),
            Pair(102, TransactionResultCode(R.string.zvt_error_code_102)),
            Pair(103, TransactionResultCode(R.string.zvt_error_code_103)),
            Pair(104, TransactionResultCode(R.string.zvt_error_code_104)),
            Pair(106, TransactionResultCode(R.string.zvt_error_code_106)),
            Pair(107, TransactionResultCode(R.string.zvt_error_code_107)),
            Pair(108, TransactionResultCode(R.string.zvt_error_code_108)),
            Pair(110, TransactionResultCode(R.string.zvt_error_code_110)),
            Pair(111, TransactionResultCode(R.string.zvt_error_code_111)),
            Pair(113, TransactionResultCode(R.string.zvt_error_code_113)),
            Pair(114, TransactionResultCode(R.string.zvt_error_code_114)),
            Pair(115, TransactionResultCode(R.string.zvt_error_code_115)),
            Pair(119, TransactionResultCode(R.string.zvt_error_code_119)),
            Pair(120, TransactionResultCode(R.string.zvt_error_code_120)),
            Pair(121, TransactionResultCode(R.string.zvt_error_code_121)),
            Pair(122, TransactionResultCode(R.string.zvt_error_code_122)),
            Pair(123, TransactionResultCode(R.string.zvt_error_code_123)),
            Pair(124, TransactionResultCode(R.string.zvt_error_code_124)),
            Pair(125, TransactionResultCode(R.string.zvt_error_code_125)),
            Pair(126, TransactionResultCode(R.string.zvt_error_code_126)),
            Pair(131, TransactionResultCode(R.string.zvt_error_code_131)),
            Pair(133, TransactionResultCode(R.string.zvt_error_code_133)),
            Pair(137, TransactionResultCode(R.string.zvt_error_code_137)),
            Pair(154, TransactionResultCode(R.string.zvt_error_code_154)),
            Pair(155, TransactionResultCode(R.string.zvt_error_code_155)),
            Pair(
                156,
                TransactionResultCode(
                    R.string.zvt_error_code_156,
                    listOf(R.string.recovery_message_wait, R.string.recovery_message_no_action)
                )
            ),
            Pair(157, TransactionResultCode(R.string.zvt_error_code_157)),
            Pair(160, TransactionResultCode(R.string.zvt_error_code_160)),
            Pair(161, TransactionResultCode(R.string.zvt_error_code_161)),
            Pair(163, TransactionResultCode(R.string.zvt_error_code_163)),
            Pair(164, TransactionResultCode(R.string.zvt_error_code_164)),
            Pair(
                177,
                TransactionResultCode(
                    R.string.zvt_error_code_177,
                    listOf(
                        R.string.recovery_message_end_of_day,
                        R.string.recovery_message_service_technician_fix
                    )
                )
            ),
            Pair(
                178,
                TransactionResultCode(
                    R.string.zvt_error_code_178,
                    listOf(
                        R.string.recovery_message_read_file,
                        R.string.recovery_message_delete_file
                    )
                )
            ),
            Pair(180, TransactionResultCode(R.string.zvt_error_code_180)),
            Pair(181, TransactionResultCode(R.string.zvt_error_code_181)),
            Pair(183, TransactionResultCode(R.string.zvt_error_code_183)),
            Pair(184, TransactionResultCode(R.string.zvt_error_code_184)),
            Pair(
                191,
                TransactionResultCode(
                    R.string.zvt_error_code_191,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(
                192,
                TransactionResultCode(
                    R.string.zvt_error_code_192,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(
                193,
                TransactionResultCode(
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
                TransactionResultCode(
                    R.string.zvt_error_code_194,
                    listOf(R.string.recovery_message_diagnosis)
                )
            ),
            Pair(195, TransactionResultCode(R.string.zvt_error_code_195)),
            Pair(
                196,
                TransactionResultCode(
                    R.string.zvt_error_code_196,
                    listOf(
                        R.string.recovery_message_software_update,
                        R.string.recovery_message_service_technician_fix
                    )
                )
            ),
            Pair(197, TransactionResultCode(R.string.zvt_error_code_197)),
            Pair(198, TransactionResultCode(R.string.zvt_error_code_198)),
            Pair(200, TransactionResultCode(R.string.zvt_error_code_200)),
            Pair(201, TransactionResultCode(R.string.zvt_error_code_201)),
            Pair(203, TransactionResultCode(R.string.zvt_error_code_203)),
            Pair(
                204,
                TransactionResultCode(
                    R.string.zvt_error_code_204,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(205, TransactionResultCode(R.string.zvt_error_code_205)),
            Pair(210, TransactionResultCode(R.string.zvt_error_code_210)),
            Pair(
                220,
                TransactionResultCode(
                    R.string.zvt_error_code_220,
                    listOf(R.string.recovery_message_proceed_or_abort)
                )
            ),
            Pair(221, TransactionResultCode(R.string.zvt_error_code_221)),
            Pair(222, TransactionResultCode(R.string.zvt_error_code_222)),
            Pair(
                223,
                TransactionResultCode(
                    R.string.zvt_error_code_223,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(
                224,
                TransactionResultCode(
                    R.string.zvt_error_code_224,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(
                225,
                TransactionResultCode(
                    R.string.zvt_error_code_225,
                    listOf(R.string.recovery_message_extract_card)
                )
            ),
            Pair(
                226,
                TransactionResultCode(
                    R.string.zvt_error_code_226,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(227, TransactionResultCode(R.string.zvt_error_code_227)),
            Pair(
                228,
                TransactionResultCode(
                    R.string.zvt_error_code_228,
                    listOf(R.string.recovery_message_service_technician_fix)
                )
            ),
            Pair(231, TransactionResultCode(R.string.zvt_error_code_231)),
            Pair(232, TransactionResultCode(R.string.zvt_error_code_232)),
            Pair(233, TransactionResultCode(R.string.zvt_error_code_233)),
            Pair(234, TransactionResultCode(R.string.zvt_error_code_234)),
            Pair(235, TransactionResultCode(R.string.zvt_error_code_235)),
            Pair(236, TransactionResultCode(R.string.zvt_error_code_236)),
            Pair(237, TransactionResultCode(R.string.zvt_error_code_237)),
            Pair(240, TransactionResultCode(R.string.zvt_error_code_240)),
            Pair(241, TransactionResultCode(R.string.zvt_error_code_241)),
            Pair(245, TransactionResultCode(R.string.zvt_error_code_245)),
            Pair(246, TransactionResultCode(R.string.zvt_error_code_246)),
            Pair(250, TransactionResultCode(R.string.zvt_error_code_250)),
            Pair(251, TransactionResultCode(R.string.zvt_error_code_251)),
            Pair(252, TransactionResultCode(R.string.zvt_error_code_252)),
            Pair(253, TransactionResultCode(R.string.zvt_error_code_253)),
            Pair(254, TransactionResultCode(R.string.zvt_error_code_254)),
            Pair(255, TransactionResultCode(R.string.zvt_error_code_255))
        )
    )

    data object LavegoResultCodes : ResultCodeSets(
        mapOf(
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
            Pair(
                64,
                TransactionResultCode(R.string.lavego_result_code_64_original_amount_incorrect)
            ),
            Pair(65, TransactionResultCode(R.string.lavego_result_code_65_no_pin_limit_exceeded)),
            Pair(75, TransactionResultCode(R.string.lavego_result_code_75_pin_try_limit_exceeded)),
            Pair(80, TransactionResultCode(R.string.lavego_result_code_80_referenced_tx_unknown)),
            Pair(
                81,
                TransactionResultCode(
                    errorMessage = R.string.lavego_result_code_81_initialisation_required,
                    recoveryMessages = listOf(R.string.recovery_message_initialisation)
                )
            ),
            Pair(84, TransactionResultCode(R.string.lavego_result_code_84_pin_not_active)),
            Pair(85, TransactionResultCode(R.string.lavego_result_code_85_cashback_not_possible)),
            Pair(91, TransactionResultCode(R.string.lavego_result_code_91_card_issuer_inoperative)),
            Pair(92, TransactionResultCode(R.string.lavego_result_code_92_invalid_card_type)),
            Pair(96, TransactionResultCode(R.string.lavego_result_code_96_system_inoperative))
        )
    )

    companion object {
        fun getZVTCode(resultCode: Int?): TransactionResultCode {
            return LavegoResultCodes.mapping.getOrDefault(
                resultCode,
                ZvtResultCodes.mapping.getOrDefault(
                    resultCode,
                    TransactionResultCode(errorMessage = R.string.zvt_error_code_unknown)
                )
            )
        }

        fun getOPICode(resultCode: Int?): TransactionResultCode {
            return OpiResultCodes.mapping.getOrDefault(
                resultCode,
                TransactionResultCode(errorMessage = R.string.zvt_error_code_unknown)
            )
        }
    }
}
