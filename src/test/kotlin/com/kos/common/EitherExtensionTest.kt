package com.kos.common

import arrow.core.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `getLeftOrNull should return left value when Either is Left`() {
        val either: Either<String, Int> = Either.Left("Error")
        val result = either.getLeftOrNull()
        assertEquals("Error", result)
    }

    @Test
    fun `getLeftOrNull should return null when Either is Right`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.getLeftOrNull()
        assertEquals(null, result)
    }

    @Test
    fun `getOrThrow should return right's value if it's right`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.getOrThrow(RuntimeException("Should not throw"))
        assertEquals(42, result)
    }

    @Test
    fun `getOrThrow should throw an exception if it's left`() {
        val either: Either<String, Int> = Either.Left("Error")
        val exception = RuntimeException("Expected exception")

        val thrown = assertFailsWith<Throwable> {
            either.getOrThrow(exception)
        }

        assertEquals(exception.message, thrown.message)
    }
}
