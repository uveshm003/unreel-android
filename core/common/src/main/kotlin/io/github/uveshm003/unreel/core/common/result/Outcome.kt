package io.github.uveshm003.unreel.core.common.result

/**
 * The result of an operation that can fail in an *expected* way, e.g. a settings change refused by the commitment
 * lock. Unexpected failures (bugs, I/O errors) stay exceptions. [E] is usually a small sealed type, so callers
 * handle every reason with an exhaustive `when`.
 */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>

    data class Failure<out E>(val error: E) : Outcome<Nothing, E>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.value

    fun errorOrNull(): E? = (this as? Failure)?.error
}

inline fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T, E, R> Outcome<T, E>.mapError(transform: (E) -> R): Outcome<T, R> = when (this) {
    is Outcome.Success -> this
    is Outcome.Failure -> Outcome.Failure(transform(error))
}

inline fun <T, E, R> Outcome<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
    is Outcome.Success -> onSuccess(value)
    is Outcome.Failure -> onFailure(error)
}

inline fun <T, E> Outcome<T, E>.onSuccess(action: (T) -> Unit): Outcome<T, E> = also {
    if (it is Outcome.Success) action(it.value)
}

inline fun <T, E> Outcome<T, E>.onFailure(action: (E) -> Unit): Outcome<T, E> = also {
    if (it is Outcome.Failure) action(it.error)
}
