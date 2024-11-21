package de.tillhub.paymentengine.data

import android.os.Parcelable
import androidx.annotation.StringRes
import de.tillhub.paymentengine.R
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
sealed class TransactionResultCode : Parcelable {
    abstract val errorMessage: Int
    abstract val recoveryMessages: List<Int>

    class Known(
        @StringRes
        override val errorMessage: Int,
        @StringRes
        override val recoveryMessages: List<Int> = listOf()
    ) : TransactionResultCode() {
        override fun equals(other: Any?) = other is Known &&
                errorMessage == other.errorMessage &&
                recoveryMessages == other.recoveryMessages

        override fun hashCode() = Objects.hash(
            errorMessage,
            recoveryMessages
        )

        override fun toString() = "TransactionResultCode.Known(" +
                "errorMessage=$errorMessage, " +
                "recoveryMessages=$recoveryMessages" +
                ")"
    }

    class Unknown(
        val resultCode: Int,
        val resultCodeString: String? = null,
        @StringRes
        override val errorMessage: Int,
        @StringRes
        override val recoveryMessages: List<Int> = listOf()
    ) : TransactionResultCode() {

        override fun equals(other: Any?) = other is Unknown &&
                resultCode == other.resultCode &&
                errorMessage == other.errorMessage &&
                recoveryMessages == other.recoveryMessages

        override fun hashCode() = Objects.hash(
            resultCode,
            errorMessage,
            recoveryMessages
        )

        override fun toString() = "TransactionResultCode.Unknown(" +
                "resultCode=$resultCode, " +
                "errorMessage=$errorMessage, " +
                "recoveryMessages=$recoveryMessages" +
                ")"
    }
}

@SuppressWarnings("MagicNumber")
internal sealed class ResultCodeSets<T>(val mapping: Map<T, TransactionResultCode>) {
    data object OpiResultCodes : ResultCodeSets<Int>(
        mapOf(
            Pair(0, TransactionResultCode.Known(R.string.opi_error_code_00)),
            Pair(1, TransactionResultCode.Known(R.string.opi_error_code_01)),
            Pair(2, TransactionResultCode.Known(R.string.opi_error_code_02)),
            Pair(3, TransactionResultCode.Known(R.string.opi_error_code_03)),
            Pair(4, TransactionResultCode.Known(R.string.opi_error_code_04)),
            Pair(5, TransactionResultCode.Known(R.string.opi_error_code_05)),
            Pair(6, TransactionResultCode.Known(R.string.opi_error_code_06)),
            Pair(7, TransactionResultCode.Known(R.string.opi_error_code_07)),
            Pair(8, TransactionResultCode.Known(R.string.opi_error_code_08)),
            Pair(9, TransactionResultCode.Known(R.string.opi_error_code_09)),
            Pair(10, TransactionResultCode.Known(R.string.opi_error_code_10)),
            Pair(11, TransactionResultCode.Known(R.string.opi_error_code_11)),
            Pair(12, TransactionResultCode.Known(R.string.opi_error_code_12)),
            Pair(13, TransactionResultCode.Known(R.string.opi_error_code_13)),
            Pair(14, TransactionResultCode.Known(R.string.opi_error_code_14)),
            Pair(15, TransactionResultCode.Known(R.string.opi_error_code_15)),
            Pair(16, TransactionResultCode.Known(R.string.opi_error_code_16)),
            Pair(17, TransactionResultCode.Known(R.string.opi_error_code_17)),
            Pair(18, TransactionResultCode.Known(R.string.opi_error_code_18)),
            Pair(19, TransactionResultCode.Known(R.string.opi_error_code_19)),
            Pair(20, TransactionResultCode.Known(R.string.opi_error_code_20)),
            Pair(21, TransactionResultCode.Known(R.string.opi_error_code_21)),
            Pair(22, TransactionResultCode.Known(R.string.opi_error_code_22)),
            Pair(23, TransactionResultCode.Known(R.string.opi_error_code_23)),
            Pair(24, TransactionResultCode.Known(R.string.opi_error_code_24)),
            Pair(25, TransactionResultCode.Known(R.string.opi_error_code_25)),
            Pair(26, TransactionResultCode.Known(R.string.opi_error_code_26)),
            Pair(27, TransactionResultCode.Known(R.string.opi_error_code_27)),
            Pair(28, TransactionResultCode.Known(R.string.opi_error_code_28)),
            Pair(29, TransactionResultCode.Known(R.string.opi_error_code_29)),
            Pair(30, TransactionResultCode.Known(R.string.opi_error_code_30)),
            Pair(31, TransactionResultCode.Known(R.string.opi_error_code_31)),
            Pair(32, TransactionResultCode.Known(R.string.opi_error_code_32)),
            Pair(33, TransactionResultCode.Known(R.string.opi_error_code_33)),
            Pair(34, TransactionResultCode.Known(R.string.opi_error_code_34)),
            Pair(35, TransactionResultCode.Known(R.string.opi_error_code_35)),
            Pair(36, TransactionResultCode.Known(R.string.opi_error_code_36)),
            Pair(37, TransactionResultCode.Known(R.string.opi_error_code_37)),
            Pair(38, TransactionResultCode.Known(R.string.opi_error_code_38)),
            Pair(39, TransactionResultCode.Known(R.string.opi_error_code_39)),
            Pair(40, TransactionResultCode.Known(R.string.opi_error_code_40)),
            Pair(41, TransactionResultCode.Known(R.string.opi_error_code_41)),
            Pair(42, TransactionResultCode.Known(R.string.opi_error_code_42)),
            Pair(43, TransactionResultCode.Known(R.string.opi_error_code_43)),
            Pair(44, TransactionResultCode.Known(R.string.opi_error_code_44)),
            Pair(51, TransactionResultCode.Known(R.string.opi_error_code_51)),
            Pair(52, TransactionResultCode.Known(R.string.opi_error_code_52)),
            Pair(53, TransactionResultCode.Known(R.string.opi_error_code_53)),
            Pair(54, TransactionResultCode.Known(R.string.opi_error_code_54)),
            Pair(55, TransactionResultCode.Known(R.string.opi_error_code_55)),
            Pair(56, TransactionResultCode.Known(R.string.opi_error_code_56)),
            Pair(57, TransactionResultCode.Known(R.string.opi_error_code_57)),
            Pair(58, TransactionResultCode.Known(R.string.opi_error_code_58)),
            Pair(59, TransactionResultCode.Known(R.string.opi_error_code_59)),
            Pair(60, TransactionResultCode.Known(R.string.opi_error_code_60)),
            Pair(61, TransactionResultCode.Known(R.string.opi_error_code_61)),
            Pair(62, TransactionResultCode.Known(R.string.opi_error_code_62)),
            Pair(63, TransactionResultCode.Known(R.string.opi_error_code_63)),
            Pair(64, TransactionResultCode.Known(R.string.opi_error_code_64)),
            Pair(65, TransactionResultCode.Known(R.string.opi_error_code_65)),
            Pair(66, TransactionResultCode.Known(R.string.opi_error_code_66)),
            Pair(67, TransactionResultCode.Known(R.string.opi_error_code_67)),
            Pair(68, TransactionResultCode.Known(R.string.opi_error_code_68)),
            Pair(75, TransactionResultCode.Known(R.string.opi_error_code_75)),
            Pair(78, TransactionResultCode.Known(R.string.opi_error_code_78)),
            Pair(80, TransactionResultCode.Known(R.string.opi_error_code_80)),
            Pair(82, TransactionResultCode.Known(R.string.opi_error_code_82)),
            Pair(85, TransactionResultCode.Known(R.string.opi_error_code_85)),
            Pair(90, TransactionResultCode.Known(R.string.opi_error_code_90)),
            Pair(91, TransactionResultCode.Known(R.string.opi_error_code_91)),
            Pair(92, TransactionResultCode.Known(R.string.opi_error_code_92)),
            Pair(93, TransactionResultCode.Known(R.string.opi_error_code_93)),
            Pair(94, TransactionResultCode.Known(R.string.opi_error_code_94)),
            Pair(95, TransactionResultCode.Known(R.string.opi_error_code_95)),
            Pair(96, TransactionResultCode.Known(R.string.opi_error_code_96)),
            Pair(100, TransactionResultCode.Known(R.string.opi_error_code_100)),
            Pair(101, TransactionResultCode.Known(R.string.opi_error_code_101)),
            Pair(102, TransactionResultCode.Known(R.string.opi_error_code_102)),
            Pair(103, TransactionResultCode.Known(R.string.opi_error_code_103)),
            Pair(104, TransactionResultCode.Known(R.string.opi_error_code_104)),
            Pair(105, TransactionResultCode.Known(R.string.opi_error_code_105)),
            Pair(106, TransactionResultCode.Known(R.string.opi_error_code_106)),
            Pair(107, TransactionResultCode.Known(R.string.opi_error_code_107)),
            Pair(108, TransactionResultCode.Known(R.string.opi_error_code_108)),
            Pair(109, TransactionResultCode.Known(R.string.opi_error_code_109)),
            Pair(110, TransactionResultCode.Known(R.string.opi_error_code_110)),
            Pair(111, TransactionResultCode.Known(R.string.opi_error_code_111)),
            Pair(112, TransactionResultCode.Known(R.string.opi_error_code_112)),
            Pair(113, TransactionResultCode.Known(R.string.opi_error_code_113)),
            Pair(114, TransactionResultCode.Known(R.string.opi_error_code_114)),
            Pair(115, TransactionResultCode.Known(R.string.opi_error_code_115)),
            Pair(116, TransactionResultCode.Known(R.string.opi_error_code_116)),
            Pair(117, TransactionResultCode.Known(R.string.opi_error_code_117)),
            Pair(118, TransactionResultCode.Known(R.string.opi_error_code_118)),
            Pair(119, TransactionResultCode.Known(R.string.opi_error_code_119)),
            Pair(120, TransactionResultCode.Known(R.string.opi_error_code_120)),
            Pair(121, TransactionResultCode.Known(R.string.opi_error_code_121)),
            Pair(122, TransactionResultCode.Known(R.string.opi_error_code_122)),
            Pair(123, TransactionResultCode.Known(R.string.opi_error_code_123)),
            Pair(124, TransactionResultCode.Known(R.string.opi_error_code_124)),
            Pair(125, TransactionResultCode.Known(R.string.opi_error_code_125)),
            Pair(126, TransactionResultCode.Known(R.string.opi_error_code_126)),
            Pair(127, TransactionResultCode.Known(R.string.opi_error_code_127)),
            Pair(128, TransactionResultCode.Known(R.string.opi_error_code_128)),
            Pair(129, TransactionResultCode.Known(R.string.opi_error_code_129)),
            Pair(200, TransactionResultCode.Known(R.string.opi_error_code_200)),
            Pair(201, TransactionResultCode.Known(R.string.opi_error_code_201)),
            Pair(202, TransactionResultCode.Known(R.string.opi_error_code_202)),
            Pair(203, TransactionResultCode.Known(R.string.opi_error_code_203)),
            Pair(204, TransactionResultCode.Known(R.string.opi_error_code_204)),
            Pair(205, TransactionResultCode.Known(R.string.opi_error_code_205)),
            Pair(206, TransactionResultCode.Known(R.string.opi_error_code_206)),
            Pair(207, TransactionResultCode.Known(R.string.opi_error_code_207)),
            Pair(208, TransactionResultCode.Known(R.string.opi_error_code_208)),
            Pair(209, TransactionResultCode.Known(R.string.opi_error_code_209)),
            Pair(210, TransactionResultCode.Known(R.string.opi_error_code_210)),
            Pair(300, TransactionResultCode.Known(R.string.opi_error_code_300)),
            Pair(301, TransactionResultCode.Known(R.string.opi_error_code_301)),
            Pair(302, TransactionResultCode.Known(R.string.opi_error_code_302)),
            Pair(303, TransactionResultCode.Known(R.string.opi_error_code_303)),
            Pair(304, TransactionResultCode.Known(R.string.opi_error_code_304)),
            Pair(305, TransactionResultCode.Known(R.string.opi_error_code_305)),
            Pair(306, TransactionResultCode.Known(R.string.opi_error_code_306)),
            Pair(307, TransactionResultCode.Known(R.string.opi_error_code_307)),
            Pair(308, TransactionResultCode.Known(R.string.opi_error_code_308)),
            Pair(309, TransactionResultCode.Known(R.string.opi_error_code_309)),
            Pair(400, TransactionResultCode.Known(R.string.opi_error_code_400)),
            Pair(500, TransactionResultCode.Known(R.string.opi_error_code_500)),
            Pair(501, TransactionResultCode.Known(R.string.opi_error_code_501)),
            Pair(502, TransactionResultCode.Known(R.string.opi_error_code_502)),
            Pair(503, TransactionResultCode.Known(R.string.opi_error_code_503)),
            Pair(504, TransactionResultCode.Known(R.string.opi_error_code_504)),
            Pair(600, TransactionResultCode.Known(R.string.opi_error_code_600)),
            Pair(601, TransactionResultCode.Known(R.string.opi_error_code_601)),
            Pair(602, TransactionResultCode.Known(R.string.opi_error_code_602)),
            Pair(603, TransactionResultCode.Known(R.string.opi_error_code_603)),
            Pair(604, TransactionResultCode.Known(R.string.opi_error_code_604)),
            Pair(605, TransactionResultCode.Known(R.string.opi_error_code_605)),
            Pair(606, TransactionResultCode.Known(R.string.opi_error_code_606)),
            Pair(700, TransactionResultCode.Known(R.string.opi_error_code_700)),
            Pair(800, TransactionResultCode.Known(R.string.opi_error_code_800)),
            Pair(900, TransactionResultCode.Known(R.string.opi_error_code_900)),
            Pair(901, TransactionResultCode.Known(R.string.opi_error_code_901)),
            Pair(902, TransactionResultCode.Known(R.string.opi_error_code_902)),
            Pair(903, TransactionResultCode.Known(R.string.opi_error_code_903)),
            Pair(904, TransactionResultCode.Known(R.string.opi_error_code_904)),
            Pair(905, TransactionResultCode.Known(R.string.opi_error_code_905)),
            Pair(906, TransactionResultCode.Known(R.string.opi_error_code_906)),
            Pair(907, TransactionResultCode.Known(R.string.opi_error_code_907)),
            Pair(908, TransactionResultCode.Known(R.string.opi_error_code_908)),
            Pair(909, TransactionResultCode.Known(R.string.opi_error_code_909)),
            Pair(910, TransactionResultCode.Known(R.string.opi_error_code_910)),
            Pair(911, TransactionResultCode.Known(R.string.opi_error_code_911)),
            Pair(912, TransactionResultCode.Known(R.string.opi_error_code_912)),
            Pair(913, TransactionResultCode.Known(R.string.opi_error_code_913)),
            Pair(914, TransactionResultCode.Known(R.string.opi_error_code_914)),
            Pair(915, TransactionResultCode.Known(R.string.opi_error_code_915)),
            Pair(916, TransactionResultCode.Known(R.string.opi_error_code_916)),
            Pair(917, TransactionResultCode.Known(R.string.opi_error_code_917)),
            Pair(918, TransactionResultCode.Known(R.string.opi_error_code_918)),
            Pair(919, TransactionResultCode.Known(R.string.opi_error_code_919)),
            Pair(920, TransactionResultCode.Known(R.string.opi_error_code_920)),
            Pair(921, TransactionResultCode.Known(R.string.opi_error_code_921)),
            Pair(922, TransactionResultCode.Known(R.string.opi_error_code_922)),
            Pair(923, TransactionResultCode.Known(R.string.opi_error_code_923)),
            Pair(950, TransactionResultCode.Known(R.string.opi_error_code_950)),
        )
    )

    data object ZvtResultCodes : ResultCodeSets<Int>(
        mapOf(
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
    )

    data object LavegoResultCodes : ResultCodeSets<Int>(
        mapOf(
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
    )

    data object SPOSResultCodes : ResultCodeSets<String>(
        mapOf(
            Pair(
                "S_SWITCH_NOT_CONNECTED",
                TransactionResultCode.Known(R.string.spos_error_terminal_not_connected)
            ),
            Pair(
                "CARD_PAYMENT_NOT_ONBOARDED",
                TransactionResultCode.Known(R.string.spos_error_terminal_not_onboarded)
            ),
            Pair(
                "Failure",
                TransactionResultCode.Known(R.string.spos_error_failure)
            ),
            Pair(
                "Aborted",
                TransactionResultCode.Known(R.string.spos_error_aborted)
            ),
            Pair(
                "Busy",
                TransactionResultCode.Known(R.string.spos_error_busy)
            ),
            Pair(
                "CommunicationError",
                TransactionResultCode.Known(R.string.spos_error_communication_error)
            ),
            Pair(
                "DeviceConfigurationFailure",
                TransactionResultCode.Known(R.string.spos_error_configuration_failure)
            ),
            Pair(
                "DeviceUnavailable",
                TransactionResultCode.Known(R.string.spos_error_device_unavailable)
            ),
            Pair(
                "FormatError",
                TransactionResultCode.Known(R.string.spos_error_format_error)
            ),
            Pair(
                "MissingMandatoryData",
                TransactionResultCode.Known(R.string.spos_error_missing_mandatory_data)
            ),
            Pair(
                "NoActivePayment",
                TransactionResultCode.Known(R.string.spos_error_no_active_payment)
            ),
            Pair(
                "ParsingError",
                TransactionResultCode.Known(R.string.spos_error_parsing_error)
            ),
            Pair(
                "PartialFailure",
                TransactionResultCode.Known(R.string.spos_error_partial_failure)
            ),
            Pair(
                "PaymentOnGoing",
                TransactionResultCode.Known(R.string.spos_error_payment_ongoing)
            ),
            Pair(
                "PcCommunicationFailed",
                TransactionResultCode.Known(R.string.spos_error_pc_communication_failed)
            ),
            Pair(
                "DeviceConfigurationFailed",
                TransactionResultCode.Known(R.string.spos_error_configuration_failure)
            ),
            Pair(
                "PrintLastTicket",
                TransactionResultCode.Known(R.string.spos_error_print_last_ticket)
            ),
            Pair(
                "TimedOut",
                TransactionResultCode.Known(R.string.spos_error_timed_out)
            ),
            Pair(
                "ReceiptCallFailed",
                TransactionResultCode.Known(R.string.spos_error_receipt_call_failed)
            ),
            Pair(
                "TerminalAlreadyActivated",
                TransactionResultCode.Known(R.string.spos_error_terminal_already_activated)
            ),
            Pair(
                "ValidationError",
                TransactionResultCode.Known(R.string.spos_error_validation_error)
            ),
            Pair(
                "Unknown",
                TransactionResultCode.Known(R.string.spos_error_unknown)
            )
        )
    )

    companion object {
        private const val UNKNOWN_RESULT_CODE = -1

        fun getZVTCode(resultCode: Int?): TransactionResultCode {
            return LavegoResultCodes.mapping.getOrDefault(
                resultCode,
                ZvtResultCodes.mapping.getOrDefault(
                    resultCode,
                    TransactionResultCode.Unknown(
                        resultCode = resultCode ?: UNKNOWN_RESULT_CODE,
                        errorMessage = R.string.zvt_error_code_unknown
                    )
                )
            )
        }

        fun getOPICode(resultCode: Int?): TransactionResultCode {
            return OpiResultCodes.mapping.getOrDefault(
                resultCode,
                TransactionResultCode.Unknown(
                    resultCode = resultCode ?: UNKNOWN_RESULT_CODE,
                    errorMessage = R.string.zvt_error_code_unknown
                )
            )
        }

        fun getSPOSCode(resultCode: String?): TransactionResultCode {
            return SPOSResultCodes.mapping.getOrDefault(
                resultCode,
                TransactionResultCode.Unknown(
                    resultCode = UNKNOWN_RESULT_CODE,
                    resultCodeString = resultCode,
                    errorMessage = R.string.zvt_error_code_unknown
                )
            )
        }
    }
}
