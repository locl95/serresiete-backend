package com.kos.common

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

    data class CustomType(val value: String)
}
