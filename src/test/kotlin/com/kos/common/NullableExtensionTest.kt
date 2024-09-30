package com.kos.common

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    data class CustomType(val value: String)
}
