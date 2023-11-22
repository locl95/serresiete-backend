package com.kos.common

import arrow.core.Either
import kotlin.test.Test
import kotlin.test.assertEquals

class EitherExtensionTest {
    @Test
    fun `split should correctly separate Left and Right values`() {
        val eitherList: List<Either<Int, String>> = listOf(
            Either.Left(42),
            Either.Right("Hello"),
            Either.Left(10),
            Either.Right("World")
        )
        val (leftValues, rightValues) = eitherList.split()
        assertEquals(listOf(42, 10), leftValues)
        assertEquals(listOf("Hello", "World"), rightValues)
    }

    @Test
    fun `split should handle an empty list`() {
        val emptyList: List<Either<Int, String>> = emptyList()
        val (leftValues, rightValues) = emptyList.split()
        assertEquals(emptyList(), leftValues)
        assertEquals(emptyList(), rightValues)
    }

    @Test
    fun `split should handle a list with only Left values`() {
        val leftList: List<Either<Int, String>> = listOf(
            Either.Left(42),
            Either.Left(10),
            Either.Left(5)
        )
        val (leftValues, rightValues) = leftList.split()
        assertEquals(listOf(42, 10, 5), leftValues)
        assertEquals(emptyList(), rightValues)
    }

    @Test
    fun `split should handle a list with only Right values`() {
        val rightList: List<Either<Int, String>> = listOf(
            Either.Right("Hello"),
            Either.Right("World")
        )
        val (leftValues, rightValues) = rightList.split()
        assertEquals(emptyList(), leftValues)
        assertEquals(listOf("Hello", "World"), rightValues)
    }
}
