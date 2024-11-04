package com.kos.common

import arrow.core.Either

fun <T> T?.isDefined(): Boolean {
    return this != null
}

suspend fun <T, R> T?.fold(left: suspend () -> R, right: suspend (T) -> R): R {
    return if (this != null) right(this)
    else left()
}

fun <T, R> T?._fold(left: () -> R, right: (T) -> R): R {
    return if (this != null) right(this)
    else left()
}

fun <T, L1, L2, R> T?.recoverToEither(recoverWith: (T) -> L2, attempt: (T) -> Either<L1, R>): Either<L2, R?> =
    this?.let { value -> attempt(value).mapLeft { recoverWith(value) } } ?: Either.Right(null)