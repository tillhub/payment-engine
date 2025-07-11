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

    companion object {
        val ACTION_NOT_SUPPORTED = Known(R.string.operation_not_supported_error)
    }
}
