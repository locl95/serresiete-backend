package com.kos.datacache

import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.datacache.TestHelper.lolDataCache
import com.kos.datacache.TestHelper.wowDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.httpclients.domain.QueueType
import com.kos.httpclients.domain.RaiderIoData
import com.kos.httpclients.domain.RiotData
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataCacheServiceTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val riotClient = mock(RiotClient::class.java)

    @Test
    fun `i can get wow data`() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(
                listOf(
                    wowDataCache,
                    wowDataCache.copy(characterId = 2, data = wowDataCache.data.replace(""""id": 1""", """"id": 2""")),
                    wowDataCache.copy(characterId = 3, data = wowDataCache.data.replace(""""id": 1""", """"id": 3"""))
                )
            )
            val service = DataCacheService(repo, raiderIoClient, riotClient)
            val data = service.getData(listOf(1, 3))
            assertTrue(data.isRight { it.size == 2 })
            assertEquals(listOf<Long>(1, 3), data.map {
                it.map { d ->
                    d as RaiderIoData
                    d.id
                }
            }.getOrNull())
        }
    }

    @Test
    fun `i can get lol data`() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(
                listOf(lolDataCache)
            )
            val service = DataCacheService(repo, raiderIoClient, riotClient)
            val data = service.getData(listOf(2))
            println(data)
            assertTrue(data.isRight { it.size == 1 })
            assertEquals(listOf(basicLolCharacter.name), data.map {
                it.map { d ->
                    d as RiotData
                    d.summonerName
                }
            }.getOrNull())
        }
    }

    @Test
    fun `i can verify that getting data returns the oldest cached data record stored`() {
        runBlocking {
            val repo = DataCacheInMemoryRepository().withState(
                listOf(
                    wowDataCache.copy(inserted = wowDataCache.inserted.minusHours(10)),
                    wowDataCache.copy(data = wowDataCache.data.replace(""""score": 0.0""", """"score": 1.0""")),
                )
            )
            val service = DataCacheService(repo, raiderIoClient, riotClient)
            val data = service.getData(listOf(1))
            assertTrue(data.isRight { it.size == 1 })
            assertEquals(listOf(0.0), data.map {
                it.map { d ->
                    d as RaiderIoData
                    d.score
                }
            }.getOrNull())
        }
    }

    @Test
    fun `i can cache wow data`() {
        runBlocking {
            `when`(raiderIoClient.get(basicWowCharacter)).thenReturn(RaiderIoMockHelper.get(basicWowCharacter))
            `when`(raiderIoClient.get(basicWowCharacter.copy(id = 2))).thenReturn(
                RaiderIoMockHelper.get(basicWowCharacter.copy(id = 2))
            )
            `when`(raiderIoClient.cutoff()).thenReturn(RaiderIoMockHelper.cutoff())
            val repo = DataCacheInMemoryRepository().withState(listOf(wowDataCache))
            val service = DataCacheService(repo, raiderIoClient, riotClient)
            assertEquals(listOf(wowDataCache), repo.state())
            service.cache(listOf(basicWowCharacter, basicWowCharacter.copy(id = 2)), Game.WOW)
            assertEquals(3, repo.state().size)
        }
    }

    @Test
    fun `i can cache lol data`() {
        runBlocking {
            `when`(riotClient.getLeagueEntriesBySummonerId(basicLolCharacter.summonerId)).thenReturn(RiotMockHelper.leagueEntries)
            `when`(riotClient.getMatchesByPuuid(basicLolCharacter.puuid, QueueType.FLEX_Q.toInt())).thenReturn(
                RiotMockHelper.matches
            )
            `when`(riotClient.getMatchesByPuuid(basicLolCharacter.puuid, QueueType.SOLO_Q.toInt())).thenReturn(
                RiotMockHelper.matches
            )
            `when`(riotClient.getMatchById(RiotMockHelper.matchId)).thenReturn(RiotMockHelper.match)
            val repo = DataCacheInMemoryRepository()
            val service = DataCacheService(repo, raiderIoClient, riotClient)
            service.cache(listOf(basicLolCharacter), Game.LOL)
            assertEquals(1, repo.state().size)
        }
    }
}