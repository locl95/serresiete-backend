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
        val repo = DataCacheDatabaseRepository()
        runBlocking { assertEquals(listOf(), repo.state()) }
        runBlocking { assertEquals(true, repo.insert(dataCache)) }
        runBlocking { assertEquals(listOf(dataCache), repo.state()) }
    }

    @Test
    override fun ICanUpdateData() {
        val repo = runBlocking { DataCacheDatabaseRepository().withState(listOf(outdatedDataCache)) }
        runBlocking { assertEquals(listOf(outdatedDataCache), repo.state()) }
        runBlocking { assertEquals(true, repo.update(dataCache)) }
        runBlocking { assertEquals(listOf(dataCache), repo.state()) }
    }

    @Test
    override fun ICanUpdateDataWithMoreThan2Characters() {
        val outdatedDataCache2 = outdatedDataCache.copy(characterId = 2)
        val repo = runBlocking { DataCacheDatabaseRepository().withState(listOf(outdatedDataCache, outdatedDataCache2)) }
        runBlocking { assertEquals(listOf(outdatedDataCache, outdatedDataCache2), repo.state()) }
        runBlocking { assertEquals(true, repo.update(dataCache)) }
        runBlocking { assertEquals(listOf(dataCache, outdatedDataCache2), repo.state()) }
    }

    @Test
    override fun ICanGetData() {
        val repo = runBlocking { DataCacheDatabaseRepository().withState(listOf(dataCache)) }
        runBlocking { assertEquals(dataCache, repo.get(1)) }
    }
}