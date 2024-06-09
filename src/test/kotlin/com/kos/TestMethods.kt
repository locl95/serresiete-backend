package com.kos

import kotlin.test.fail
import kotlin.test.assertTrue

fun assertTrue(boolean: Boolean?) = when (boolean) {
    null -> fail()
    else -> assertTrue(boolean)
}