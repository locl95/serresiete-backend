package com.kos.common

import kotlin.test.Test
import kotlin.test.assertEquals

class ListCollectTest {

    private val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    @Test
    fun `must collect even numbers and square them`() {
        val result1 = numbers.collect(
            filter = { it % 2 == 0 },
            map = { it * it }
        )
        assertEquals(listOf(4, 16, 36, 64, 100), result1)
    }

    @Test
    fun `must collect odd numbers and double them`() {
        val result = numbers.collect(
            filter = { it % 2 != 0 },
            map = { it * 2 }
        )
        assertEquals(listOf(2, 6, 10, 14, 18), result)
    }

    @Test
    fun `must collect all numbers and add one to them`() {
        val result = numbers.collect(
            filter = { true },
            map = { it + 1 }
        )
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11), result)
    }
}
