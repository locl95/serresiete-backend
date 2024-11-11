package com.kos.common

import arrow.core.Either
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class NullableExtensionTest {
    @Test
    fun `isDefined should return true for non-null values`() {
        val nonNullValue: String = "Hello"
        val result = nonNullValue.isDefined()
        assertTrue(result)
    }

    @Test
    fun `isDefined should return false for null values`() {
        val nullValue: String? = null
        val result = nullValue.isDefined()
        assertFalse(result)
    }

    @Test
    fun `isDefined should return true for non-null values of custom type`() {
        val nonNullValue: CustomType = CustomType("Example")
        val result = nonNullValue.isDefined()
        assertTrue(result)
    }

    @Test
    fun `isDefined should return false for null values of custom type`() {
        val nullValue: CustomType? = null
        val result = nullValue.isDefined()
        assertFalse(result)
    }

    @Test
    fun `should call right when the receiver is not null`() {
        runBlocking {
            val value: Int = 42
            val result = value.fold(left = { "Left" }, right = { it.toString() })
            assertEquals("42", result)
        }
    }

    @Test
    fun `should call left when the receiver is null`() {
        runBlocking {
            val value: Int? = null
            val result = value.fold(left = { "Left" }, right = { it.toString() })
            assertEquals(result, "Left")
        }
    }

    @Test
    fun `recoverToEither should return right of null when value is null`() {
        val result = null.recoverToEither({ "Recovered" }) { Either.Left("Failed") }
        assertTrue(result is Either.Right)
        kotlin.test.assertEquals(result, Either.Right(null))
    }

    @Test
    fun `recoverToEither  should return left when attempt over a value fails`() {
        val input = "Input"
        val result = input.recoverToEither(
            recoverWith = { "Recovered: $it" },
            attempt = { Either.Left("Failed") }
        )

        assertTrue(result is Either.Left)
        kotlin.test.assertEquals(result, Either.Left("Recovered: Input"))
    }

    @Test
    fun `recoverToEither should return right with transformed value when attempt over a value succeeds`() {
        val input = "Input"
        val result = input.recoverToEither(
            recoverWith = { "Recovered: $it" },
            attempt = { Either.Right("Success") }
        )

        assertTrue(result is Either.Right)
        kotlin.test.assertEquals(result, Either.Right("Success"))
    }

    data class CustomType(val value: String)
}
