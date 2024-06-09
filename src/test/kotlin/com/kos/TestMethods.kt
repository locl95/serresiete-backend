package com.kos

import kotlin.test.fail
import kotlin.test.assertTrue
import kotlin.test.assertFalse

fun assertTrue(boolean: Boolean?) = when (boolean) {
    null -> fail()
    else -> assertTrue(boolean)
}

fun assertFalse(boolean: Boolean?) =  when (boolean) {
    null -> fail()
    else -> assertFalse(boolean)
}