package com.kos.datacache

import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import kotlin.test.Test
import kotlin.test.assertEquals

class DataCacheInMemoryRepositoryTest : DataCacheRepositoryTest {

    @Test
    override fun ICanInsertData() {
        val repo = DataCacheInMemoryRepository()
        assertEquals(listOf(), repo.state())
        assertEquals(true, repo.insert(dataCache))
        assertEquals(listOf(dataCache), repo.state())
    }

    @Test
    override fun ICanUpdateData() {
        val repo = DataCacheInMemoryRepository(listOf(outdatedDataCache))
        assertEquals(listOf(outdatedDataCache), repo.state())
        assertEquals(true, repo.update(dataCache))
        assertEquals(listOf(dataCache), repo.state())
    }

    @Test
    override fun ICanGetData() {
        val repo = DataCacheInMemoryRepository(listOf(dataCache))
        assertEquals(dataCache, repo.get(1))
    }
}