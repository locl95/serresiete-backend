package com.kos.datacache

import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class DataCacheInMemoryRepositoryTest : DataCacheRepositoryTest {

    @Test
    override fun ICanInsertData() {
        val repo = DataCacheInMemoryRepository()
        runBlocking {
            assertEquals(listOf(), repo.state())
            assertEquals(true, repo.insert(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }

    @Test
    override fun ICanUpdateData() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(listOf(outdatedDataCache))
            assertEquals(listOf(outdatedDataCache), repo.state())
            assertEquals(true, repo.update(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }

    @Test
    override fun ICanUpdateDataWithMoreThan2Characters() {
        val outdatedDataCache2 = outdatedDataCache.copy(characterId = 2)
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(listOf(outdatedDataCache, outdatedDataCache2))
            assertEquals(listOf(outdatedDataCache, outdatedDataCache2), repo.state())
            assertEquals(true, repo.update(dataCache))
            assertEquals(listOf(outdatedDataCache2, dataCache), repo.state())
        }
    }

    @Test
    override fun ICanGetData() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(listOf(dataCache))
            assertEquals(dataCache, repo.get(1))
        }
    }
}