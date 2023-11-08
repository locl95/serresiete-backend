package com.kos.common

fun <T> T?.isDefined(): Boolean {
    return this != null
}