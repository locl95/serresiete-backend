package com.kos.tasks

import com.kos.auth.AuthService
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.datacache.DataCacheService
import com.kos.datacache.RaiderIoMockHelper
import com.kos.datacache.RiotMockHelper
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.tasks.repository.TasksInMemoryRepository
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TasksServiceTest {
    private val raiderIoClient = Mockito.mock(RaiderIoClient::class.java)
    private val riotClient = Mockito.mock(RiotClient::class.java)

    @Test
    fun `token cleanup task should cleanup tokens`() {
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository().withState(
                listOf(
                    basicAuthorization,
                    basicAuthorization.copy(validUntil = OffsetDateTime.now().minusHours(1))
                )
            )
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            service.tokenCleanup()

            val insertedTask = tasksRepository.state().first()

            assertEquals(listOf(basicAuthorization), authRepository.state())
            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.TOKEN_CLEANUP_TASK, insertedTask.type)
        }
    }

    @Test
    fun `data cache wow task should cache wow characters`() {
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository =
                CharactersInMemoryRepository().withState(CharactersState(listOf(basicWowCharacter), listOf()))
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository()
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            `when`(raiderIoClient.get(basicWowCharacter)).thenReturn(RaiderIoMockHelper.get(basicWowCharacter))
            `when`(raiderIoClient.cutoff()).thenReturn(RaiderIoMockHelper.cutoff())

            service.cacheDataTask(Game.WOW, TaskType.CACHE_WOW_DATA_TASK)

            val insertedTask = tasksRepository.state().first()

            assertEquals(1, dataCacheRepository.state().size)
            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.CACHE_WOW_DATA_TASK, insertedTask.type)
        }
    }

    @Test
    fun `data cache lol task should cache lol characters`() {
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository =
                CharactersInMemoryRepository().withState(CharactersState(listOf(), listOf(basicLolCharacter)))
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository()
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            `when`(riotClient.getLeagueEntriesBySummonerId(basicLolCharacter.summonerId)).thenReturn(RiotMockHelper.leagueEntries)
            `when`(riotClient.getMatchesByPuuid(basicLolCharacter.puuid)).thenReturn(RiotMockHelper.matches)
            `when`(riotClient.getMatchById(RiotMockHelper.matchId)).thenReturn(RiotMockHelper.match)

            service.cacheDataTask(Game.LOL, TaskType.CACHE_LOL_DATA_TASK)

            val insertedTask = tasksRepository.state().first()

            assertEquals(1, dataCacheRepository.state().size)
            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.CACHE_LOL_DATA_TASK, insertedTask.type)
        }
    }

    @Test
    fun `run task with correct parameters should run token cleanup task`(){
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository()
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            service.runTask(TaskType.TOKEN_CLEANUP_TASK)

            val insertedTask = tasksRepository.state().first()

            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.TOKEN_CLEANUP_TASK, insertedTask.type)
        }
    }

    @Test
    fun `run task with correct parameters should run wow data cache task`(){
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository()
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            `when`(raiderIoClient.cutoff()).thenReturn(RaiderIoMockHelper.cutoff())
            service.runTask(TaskType.CACHE_WOW_DATA_TASK)

            val insertedTask = tasksRepository.state().first()

            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.CACHE_WOW_DATA_TASK, insertedTask.type)
        }
    }

    @Test
    fun `run task with correct parameters should run lol data cache task`(){
        runBlocking {
            val dataCacheRepository = DataCacheInMemoryRepository()
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)
            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val authRepository = AuthInMemoryRepository()
            val authService = AuthService(authRepository)

            val tasksRepository = TasksInMemoryRepository()
            val service = TasksService(tasksRepository, dataCacheService, charactersService, authService)

            service.runTask(TaskType.CACHE_LOL_DATA_TASK)

            val insertedTask = tasksRepository.state().first()

            assertEquals(1, tasksRepository.state().size)
            assertEquals(Status.SUCCESSFUL, decodeFromString<TaskStatus>(insertedTask.taskStatus).status)
            assertEquals(TaskType.CACHE_LOL_DATA_TASK, insertedTask.type)
        }
    }
}