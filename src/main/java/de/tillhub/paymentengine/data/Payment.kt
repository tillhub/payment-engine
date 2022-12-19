package de.tillhub.paymentengine.data

sealed class Payment<out T> {

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    class Error<T : Any>(val message: String = "", val reason: Throwable? = null) : Payment<T>() {
        override fun toString(): String = "Outcome.Error(message='$message', reason=$reason)"
    }

    class Success<T : Any>(val data: T) : Payment<T>() {
        override fun toString(): String = "OutcomeSuccess(data=$data)"
    }

    companion object {
        fun success() = Success(Unit)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> Payment<T>.getOrNull(): T? = when (this) {
    is Payment.Success -> data
    is Payment.Error -> null
}

fun <T : Any> T?.errorIfNull(errorMessage: String = ""): Payment<T> =
    when (this) {
        null -> Payment.Error(errorMessage)
        else -> Payment.Success(this)
    }