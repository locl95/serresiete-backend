package com.kos.views

import com.kos.characters.CharacterRequest
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.datacache.DataCacheService
import com.kos.views.ViewsTestHelper.basicSimpleView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.repository.ViewsInMemoryRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsServiceTest {
    private val raiderIoClient = RaiderIoMockClient()

    @Test
    fun ICanGetOwnViews() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertEquals(listOf(basicSimpleView), service.getOwnViews(owner))
        }
    }

    @Test
    fun ICanGetASimpleView() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertEquals(basicSimpleView, service.getSimple("1"))
        }
    }

    @Test
    fun ICanCreateViews() {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf())
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().isEmpty())
            assertTrue(service.create(owner, ViewRequest(name, listOf())).isRight())
            assertTrue(viewsRepository.state().size == 1)
            assertTrue(viewsRepository.state().all { it.owner == owner })
        }
    }

    @Test
    fun ICantCreateMoreThanMaximumViews() {
        runBlocking {
            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleView, basicSimpleView.copy(id = "2")))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)

            assertTrue(viewsRepository.state().size == 2)
            assertTrue(viewsRepository.state().all { it.owner == owner })
            assertTrue(service.create(owner, ViewRequest(name, listOf())).isLeft())
            assertTrue(viewsRepository.state().size == 2)
        }
    }

    @Test
    fun ICanEditAViewModifyingMoreThanOneCharacter(): Unit {
        runBlocking {
            val viewsRepository =
                ViewsInMemoryRepository().withState(listOf(basicSimpleView.copy(characterIds = listOf(1))))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 })
            assertEquals(
                service.edit(
                    id, ViewRequest(
                        name, listOf(
                            CharacterRequest("a", "r", "r"),
                            CharacterRequest("b", "r", "r"),
                            CharacterRequest("c", "r", "r"),
                            CharacterRequest("d", "r", "r")
                        )
                    )
                ), ViewModified(id, listOf(1,2,3,4))
            )
            assertTrue(viewsRepository.state().all { it.characterIds.size == 4 })
        }
    }

    @Test
    fun ICanDeleteAView(): Unit {
        runBlocking {
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView.copy(characterIds = listOf(1))))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            assertTrue(viewsRepository.state().size == 1)
            assertEquals(service.delete("1"), ViewDeleted("1"))
            assertTrue(viewsRepository.state().isEmpty())
        }
    }
}