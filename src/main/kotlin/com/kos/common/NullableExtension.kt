package com.kos.common

fun <T> T?.isDefined(): Boolean {
    return this != null
}

suspend fun <T, R> T?.fold(left: () -> R, right: suspend (T) -> R): R {
    return if (this != null) right(this)
    else left()
}