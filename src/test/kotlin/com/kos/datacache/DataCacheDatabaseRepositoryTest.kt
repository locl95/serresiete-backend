package com.kos.datacache

import com.kos.common.DatabaseFactory
import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import com.kos.datacache.repository.DataCacheDatabaseRepository
import com.kos.datacache.repository.DataCacheInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class DataCacheDatabaseRepositoryTest : DataCacheRepositoryTest {

    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }

    @Test
    override fun ICanInsertData() {
        runBlocking {
            val repo = DataCacheDatabaseRepository()
            assertEquals(listOf(), repo.state())
            assertEquals(true, repo.insert(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }

    @Test
    override fun ICanUpdateData() {
        runBlocking {
            val repo = DataCacheDatabaseRepository().withState(listOf(outdatedDataCache))
            assertEquals(listOf(outdatedDataCache), repo.state())
            assertEquals(true, repo.update(dataCache))
            assertEquals(listOf(dataCache), repo.state())
        }
    }

    @Test
    override fun ICanUpdateDataWithMoreThan2Characters() {
        runBlocking {
            val outdatedDataCache2 = outdatedDataCache.copy(characterId = 2)
            val repo = DataCacheDatabaseRepository().withState(listOf(outdatedDataCache, outdatedDataCache2))
            assertEquals(listOf(outdatedDataCache, outdatedDataCache2), repo.state())
            assertEquals(true, repo.update(dataCache))
            assertEquals(listOf(dataCache, outdatedDataCache2), repo.state())
        }
    }

    @Test
    override fun ICanGetData() {
        runBlocking {
            val repo = DataCacheDatabaseRepository().withState(listOf(dataCache))
            assertEquals(dataCache, repo.get(1))
        }
    }
}