package com.kos.views

import com.kos.characters.WowCharacterRequest
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.datacache.DataCacheService
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.ViewsTestHelper.basicSimpleLolView
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.ViewsTestHelper.published
import com.kos.views.repository.ViewsInMemoryRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

//TODO: Add testing for:
//TODO: getCachedData(simpleView: SimpleView)
//TODO: getData when view is from WOW
//TODO: getData when view is from LOL

class ViewsServiceTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val riotClient = mock(RiotClient::class.java)

    @Test
    fun `i can get own views`() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleWowView))
            val charactersRepository = CharactersInMemoryRepository().withState(emptyCharactersState)
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertEquals(listOf(basicSimpleWowView), service.getOwnViews(owner))
        }
    }

    @Test
    fun `i can get a simple view`() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleWowView))
            val charactersRepository = CharactersInMemoryRepository().withState(emptyCharactersState)
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertEquals(basicSimpleWowView, service.getSimple("1"))
        }
    }

    @Test
    fun `i can create views`() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository()
            val charactersRepository = CharactersInMemoryRepository().withState(emptyCharactersState)
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().isEmpty())
            assertTrue(service.create(owner, ViewRequest(name, published, listOf(), Game.WOW)).isRight())
            assertTrue(viewsRepository.state().size == 1)
            assertTrue(viewsRepository.state().all { it.owner == owner })
            assertTrue(viewsRepository.state().all { it.game == Game.WOW })
        }
    }

    @Test
    fun `i can create a lol view`() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository()
            val charactersRepository = CharactersInMemoryRepository().withState(emptyCharactersState)
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().isEmpty())
            assertTrue(service.create(owner, ViewRequest(name, published, listOf(), Game.LOL)).isRight())
            assertTrue(viewsRepository.state().size == 1)
            assertTrue(viewsRepository.state().all { it.owner == owner })
            assertTrue(viewsRepository.state().all { it.game == Game.LOL })
        }
    }

    @Test
    fun `i can edit a lol view`() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleLolView))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            val newName = "new-name"
            assertTrue(
                service.edit(basicSimpleLolView.id, ViewRequest(newName, published, listOf(), Game.LOL)).isRight()
            )
            assertTrue(viewsRepository.state().size == 1)
            assertTrue(viewsRepository.state().all { it.owner == owner })
            assertTrue(viewsRepository.state().all { it.name == newName })
            assertTrue(viewsRepository.state().all { it.game == Game.LOL })
        }
    }

    @Test
    fun `i cant create more than maximum views`() {
        runBlocking {
            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleWowView, basicSimpleWowView.copy(id = "2")))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)

            assertTrue(viewsRepository.state().size == 2)
            assertTrue(viewsRepository.state().all { it.owner == owner })
            assertTrue(service.create(owner, ViewRequest(name, published, listOf(), Game.WOW)).isLeft())
            assertTrue(viewsRepository.state().size == 2)
        }
    }

    @Test
    fun `i can edit a view modifying more than one character`(): Unit {
        runBlocking {

            val request1 = WowCharacterRequest("a", "r", "r")
            val request2 = WowCharacterRequest("b", "r", "r")
            val request3 = WowCharacterRequest("c", "r", "r")
            val request4 = WowCharacterRequest("d", "r", "r")

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)
            `when`(raiderIoClient.exists(request3)).thenReturn(true)
            `when`(raiderIoClient.exists(request4)).thenReturn(true)

            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleWowView.copy(characterIds = listOf(1))))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 })

            service.edit(
                id, ViewRequest(name, published, listOf(request1, request2, request3, request4), Game.WOW)
            ).fold({ fail() }) { assertEquals(ViewModified(id, listOf(1, 2, 3, 4)), it) }

            assertTrue(viewsRepository.state().all { it.characterIds.size == 4 })
        }
    }

    @Test
    fun `i can delete a view`(): Unit {
        runBlocking {
            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleWowView.copy(characterIds = listOf(1))))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().size == 1)
            assertEquals(service.delete("1"), ViewDeleted("1"))
            assertTrue(viewsRepository.state().isEmpty())
        }
    }

    @Test
    fun `i can patch a view`() {
        runBlocking {
            val patchedName = "new-name"
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleWowView))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().size == 1)
            val patch = service.patch(basicSimpleWowView.id, ViewPatchRequest(patchedName, null, null, Game.WOW))
            val patchedView = viewsRepository.state().first()
            assertEquals(patchedName, patchedView.name)
            assertEquals(patch.getOrNull(), ViewModified(basicSimpleWowView.id, basicSimpleWowView.characterIds))
        }
    }

    @Test
    fun `i can patch a view modifying more than one character`(): Unit {
        runBlocking {

            val request1 = WowCharacterRequest("a", "r", "r")
            val request2 = WowCharacterRequest("b", "r", "r")
            val request3 = WowCharacterRequest("c", "r", "r")
            val request4 = WowCharacterRequest("d", "r", "r")

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)
            `when`(raiderIoClient.exists(request3)).thenReturn(true)
            `when`(raiderIoClient.exists(request4)).thenReturn(true)

            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleWowView.copy(characterIds = listOf(1))))
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 })

            service.patch(
                id, ViewPatchRequest(null, null, listOf(request1, request2, request3, request4), Game.WOW)
            ).fold({ fail() }) { assertEquals(ViewModified(id, listOf(1, 2, 3, 4)), it) }

            assertTrue(viewsRepository.state().all { it.characterIds.size == 4 })
        }
    }
}