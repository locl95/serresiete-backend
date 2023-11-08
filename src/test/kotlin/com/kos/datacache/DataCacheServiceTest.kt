package com.kos.datacache

import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class DataCacheServiceTest {
    @Test
    fun ICanInsertDataWhenEmpty() {
        runBlocking {
            val repo = DataCacheInMemoryRepository()
            val service = DataCacheService(repo)
            assertEquals(listOf(), repo.state())
            assertEquals(true, service.insert(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }

    @Test
    fun ICanInsertDataWhenNonEmpty() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(listOf(outdatedDataCache))
            val service = DataCacheService(repo)
            assertEquals(listOf(outdatedDataCache), repo.state())
            assertEquals(true, service.insert(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }
}