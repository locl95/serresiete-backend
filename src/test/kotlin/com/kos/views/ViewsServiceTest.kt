package com.kos.views

import com.kos.characters.CharacterRequest
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.datacache.DataCacheService
import com.kos.views.repository.ViewsInMemoryRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsServiceTest {
    private val simpleView = SimpleView("1", "owner", listOf())
    private val raiderIoClient = RaiderIoMockClient()

    @Test
    fun ICanGetOwnViews() {
        val viewsRepository = ViewsInMemoryRepository(listOf(simpleView))
        val charactersRepository = CharactersInMemoryRepository(listOf())
        val charactersService = CharactersService(charactersRepository)
        val dataCacheRepository = DataCacheInMemoryRepository(listOf())
        val dataCacheService = DataCacheService(dataCacheRepository)
        val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)

        runBlocking { assertEquals(listOf(simpleView), service.getOwnViews("owner")) }
    }

    @Test
    fun ICanGetASimpleView() {
        val viewsRepository = ViewsInMemoryRepository(listOf(simpleView))
        val charactersRepository = CharactersInMemoryRepository(listOf())
        val charactersService = CharactersService(charactersRepository)
        val dataCacheRepository = DataCacheInMemoryRepository(listOf())
        val dataCacheService = DataCacheService(dataCacheRepository)
        val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)

        runBlocking { assertEquals(simpleView, service.getSimple("1")) }
    }

    @Test
    fun ICanCreateViews() {
        val viewsRepository = ViewsInMemoryRepository(listOf())
        val charactersRepository = CharactersInMemoryRepository(listOf())
        val charactersService = CharactersService(charactersRepository)
        val dataCacheRepository = DataCacheInMemoryRepository(listOf())
        val dataCacheService = DataCacheService(dataCacheRepository)
        val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
        runBlocking {
            assertTrue(viewsRepository.state().isEmpty())
            assertTrue(service.create("owner", listOf()).isRight())
            assertTrue(viewsRepository.state().size == 1)
            assertTrue(viewsRepository.state().all { it.owner == "owner" })
        }
    }

    @Test
    fun ICantCreateMoreThanMaximumViews() {
        val viewsRepository = ViewsInMemoryRepository(listOf(simpleView, simpleView.copy(id = "2")))
        val charactersRepository = CharactersInMemoryRepository(listOf())
        val charactersService = CharactersService(charactersRepository)
        val dataCacheRepository = DataCacheInMemoryRepository(listOf())
        val dataCacheService = DataCacheService(dataCacheRepository)
        val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)

        runBlocking {
            assertTrue(viewsRepository.state().size == 2)
            assertTrue(viewsRepository.state().all { it.owner == "owner" })
            assertTrue(service.create("owner", listOf()).isLeft())
            assertTrue(viewsRepository.state().size == 2)
        }
    }

    @Test
    fun ICanEditAViewModifyingMoreThanOneCharacter(): Unit {
        val viewsRepository = ViewsInMemoryRepository(listOf(simpleView.copy(characterIds = listOf(1))))
        val charactersRepository = CharactersInMemoryRepository(listOf())
        val charactersService = CharactersService(charactersRepository)
        val dataCacheRepository = DataCacheInMemoryRepository(listOf())
        val dataCacheService = DataCacheService(dataCacheRepository)
        val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
        runBlocking {
            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 } )
            assertTrue(service.edit("1", ViewRequest(listOf(
                CharacterRequest("a", "r", "r"),
                CharacterRequest("b", "r", "r"),
                CharacterRequest("c", "r", "r"),
                CharacterRequest("d", "r", "r")
            ))).isRight())
            assertTrue(viewsRepository.state().all {it.characterIds.size == 4})
        }
    }
}