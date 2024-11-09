package com.kos.common

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicCacheTest {

    @Test
    fun `cache should return the correct values and increment hits and misses`() {

        runBlocking {
            val cache = DynamicCache<String>()

            val id1 = "testId1"
            val result1 = cache.get(id1) { "value1" }
            assertEquals("value1", result1)
            assertEquals(0.0, cache.hitRate, "Hit rate should be 0 after first miss")
            assertEquals(1, cache.numberOfAccess, "Number of Access should be 1 after first fetch")

            val result2 = cache.get(id1) { "newValue1" }
            assertEquals("value1", result2, "Cached value should be returned")
            assertEquals(0.5, cache.hitRate, "Hit rate should be 50% after one hit and one miss")
            assertEquals(2, cache.numberOfAccess, "Number of Access should be 2 after second fetch")

            val id2 = "testId2"
            val result3 = cache.get(id2) { "value2" }
            assertEquals("value2", result3)
            assertEquals(1.0 / 3, cache.hitRate, "Hit rate should be 33.3% after two misses and one hit")
            assertEquals(3, cache.numberOfAccess, "Number of Access should be 3 after third fetch")

        }
    }

    @Test
    fun `cache should be thread-safe`() {
        runBlocking {
            val cache = DynamicCache<String>()
            val id = "testId"

            val results = (1..10).map {
                async {
                    cache.get(id) { "threadSafeValue" }
                }
            }.awaitAll()

            results.forEach {
                assertEquals("threadSafeValue", it)
            }

            assertEquals(10, cache.numberOfAccess, "Number of Access should be 10 after fetches")
            assertEquals(9.0 / 10.0, cache.hitRate, "Hit rate should be 90% after one miss and nine hits")
        }
    }

    @Test
    fun `hit rate should be zero with no fetches`() {
        val cache = DynamicCache<String>()
        assertEquals(0.0, cache.hitRate, "Hit rate should be zero if no requests have been made")
    }
}