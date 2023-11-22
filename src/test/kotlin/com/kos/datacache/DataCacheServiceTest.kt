package com.kos.datacache

import arrow.core.Either
import com.kos.characters.CharactersTestHelper.basicCharacter
import com.kos.datacache.TestHelper.dataCache
import com.kos.datacache.TestHelper.outdatedDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.views.RaiderIoMockClient
import com.kos.views.ViewsTestHelper.basicSimpleView
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataCacheServiceTest {
    private val raiderIoClient = RaiderIoMockClient()

    @Test
    fun ICanGetData() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(
                listOf(
                    dataCache,
                    dataCache.copy(characterId = 2, data = dataCache.data.replace(""""id": 1""", """"id": 2""")),
                    dataCache.copy(characterId = 3, data = dataCache.data.replace(""""id": 1""", """"id": 3"""))
                )
            )
            val service = DataCacheService(repo, raiderIoClient)
            val data = service.getData(basicSimpleView.copy(characterIds = listOf(1, 3)))
            assertTrue(data.isRight { it.size == 2 })
            assertEquals(listOf<Long>(1, 3), data.map { it.map { d -> d.id } }.getOrNull())
        }
    }

    @Test
    fun ICanGetDataReturnsOlderRecord() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(
                listOf(
                    dataCache.copy(inserted = dataCache.inserted.minusHours(10)),
                    dataCache.copy(data = dataCache.data.replace(""""score": 0.0""", """"score": 1.0""")),
                )
            )
            val service = DataCacheService(repo, raiderIoClient)
            val data = service.getData(basicSimpleView.copy(characterIds = listOf(1)))
            assertTrue(data.isRight { it.size == 1 })
            assertEquals(listOf(0.0), data.map { it.map { d -> d.score } }.getOrNull())
        }
    }

    @Test
    fun ICanCacheData() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(listOf(dataCache))
            val service = DataCacheService(repo, raiderIoClient)
            assertEquals(listOf(dataCache), repo.state())
            service.cache(listOf(basicCharacter, basicCharacter.copy(id = 2)))
            assertEquals(3, repo.state().size)
        }
    }
}