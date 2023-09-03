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
        val repo = DataCacheInMemoryRepository()
        val service = DataCacheService(repo)

        runBlocking { assertEquals(listOf(), repo.state()) }
        runBlocking { assertEquals(true, service.insert(dataCache)) }
        runBlocking { assertEquals(listOf(dataCache), repo.state()) }
    }

    @Test
    fun ICanInsertDataWhenNonEmpty() {
        val repo = DataCacheInMemoryRepository(listOf(outdatedDataCache))
        val service = DataCacheService(repo)

        runBlocking { assertEquals(listOf(outdatedDataCache), repo.state()) }
        runBlocking { assertEquals(true, service.insert(dataCache)) }
        runBlocking { assertEquals(listOf(dataCache), repo.state()) }
    }
}