package com.kos.datacache

import com.kos.common.DatabaseFactory
import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import com.kos.datacache.repository.DataCacheDatabaseRepository
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.datacache.repository.DataCacheRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class DataCacheRepositoryTestCommon {

    abstract val repository: DataCacheRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    open fun ICanInsertData() {
        runBlocking {
            assertEquals(listOf(), repository.state())
            assertEquals(true, repository.insert(dataCache))
            assertEquals(listOf(dataCache), repository.state())
        }
    }

    @Test
    open fun ICanUpdateData() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(outdatedDataCache))
            assertEquals(listOf(outdatedDataCache), repositoryWithState.state())
            assertEquals(true, repositoryWithState.update(dataCache))
            assertEquals(listOf(dataCache), repositoryWithState.state())
        }
    }

    @Test
    open fun ICanUpdateDataWithMoreThan2Characters() {
        runBlocking {
            val outdatedDataCache2 = outdatedDataCache.copy(characterId = 2)
            val repositoryWithState = repository.withState(listOf(outdatedDataCache, outdatedDataCache2))
            assertEquals(listOf(outdatedDataCache, outdatedDataCache2), repositoryWithState.state())
            assertEquals(true, repositoryWithState.update(dataCache))
            assertEquals(listOf(dataCache, outdatedDataCache2), repositoryWithState.state())
        }
    }

    @Test
    open fun ICanGetData() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(dataCache))
            assertEquals(listOf(dataCache), repositoryWithState.get(1))
        }
    }

    @Test
    open fun ICanGetDataReturnsDataOnlyFromTheCharacter() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(dataCache, dataCache.copy(characterId = 2)))
            assertEquals(listOf(dataCache), repositoryWithState.get(1))
        }
    }

    @Test
    open fun ICanClearData() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(dataCache, outdatedDataCache))
            assertEquals(1, repositoryWithState.deleteExpiredRecord(24))
            assertEquals(listOf(dataCache), repositoryWithState.state())
        }
    }
}

class DataCacheInMemoryRepositoryTest : DataCacheRepositoryTestCommon() {
    override val repository = DataCacheInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class DataCacheDatabaseRepositoryTest : DataCacheRepositoryTestCommon() {
    override val repository: DataCacheRepository = DataCacheDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
