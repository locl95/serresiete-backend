package com.kos.datacache

import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import kotlin.test.Test
import kotlin.test.assertEquals

class DataCacheServiceTest {
    @Test
    fun ICanInsertDataWhenEmpty() {
        val repo = DataCacheInMemoryRepository()
        val service = DataCacheService(repo)

        assertEquals(listOf(), repo.state())
        assertEquals(true,service.insert(dataCache))
        assertEquals(listOf(dataCache), repo.state())
    }
    @Test
    fun ICanInsertDataWhenNonEmpty() {
        val repo = DataCacheInMemoryRepository(listOf(outdatedDataCache))
        val service = DataCacheService(repo)

        assertEquals(listOf(outdatedDataCache), repo.state())
        assertEquals(true,service.insert(dataCache))
        assertEquals(listOf(dataCache), repo.state())
    }
}