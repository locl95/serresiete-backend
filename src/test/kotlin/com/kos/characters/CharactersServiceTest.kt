package com.kos.characters

import arrow.core.Either
import com.kos.characters.CharactersTestHelper.basicCharacter
import com.kos.characters.CharactersTestHelper.basicGetPuuidResponse
import com.kos.characters.CharactersTestHelper.basicGetSummonerResponse
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.raiderio.RaiderIoClient
import com.kos.riot.GetPUUIDResponse
import com.kos.riot.GetSummonerResponse
import com.kos.riot.RiotClient
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CharactersServiceTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val riotClient = mock(RiotClient::class.java)

    @Test
    fun `inserting two characters over an empty repository returns the ids of both new characters`() {
        runBlocking {
            val request1 = WowCharacterRequest(basicCharacter.name, basicCharacter.region, basicCharacter.realm)
            val request2 = WowCharacterRequest("kakarøna", basicCharacter.region, basicCharacter.realm)

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)

            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)


            val request = listOf(request1, request2)
            val expected: List<Long> = listOf(1, 2)

            charactersService.createAndReturnIds(request, Game.WOW).fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `inserting a character that does not exist does not get inserted`() {
        runBlocking {
            val request1 = WowCharacterRequest(basicCharacter.name, basicCharacter.region, basicCharacter.realm)
            val request2 = WowCharacterRequest("kakarøna", basicCharacter.region, basicCharacter.realm)

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(false)

            val charactersRepository = CharactersInMemoryRepository()
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)


            val request = listOf(request1, request2)
            val expected: List<Long> = listOf(1)
            charactersService.createAndReturnIds(request, Game.WOW).fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `it should skip inserting same league character even if he changed his name`() {
        runBlocking {

            val state = CharactersState(listOf(), listOf(basicLolCharacter))
            val request = LolCharacterRequest("R7 Disney Girl", "EUW")

            `when`(riotClient.getPUUIDByRiotId("R7 Disney Girl", "EUW")).thenReturn(Either.Right(basicGetPuuidResponse))
            `when`(riotClient.getSummonerByPuuid("999")).thenReturn(Either.Right(basicGetSummonerResponse))

            val charactersRepository = CharactersInMemoryRepository().withState(state)
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            val expected: List<Long> = listOf()
            val createAndReturnIds = charactersService.createAndReturnIds(listOf(request), Game.LOL)
            createAndReturnIds.fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `i can get a character`() {
        runBlocking {
            val charactersRepository =
                CharactersInMemoryRepository().withState(CharactersState(listOf(basicCharacter), listOf()))
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            assertEquals(basicCharacter, charactersService.get(basicCharacter.id, Game.WOW))
        }
    }

    @Test
    fun `i can get all characters`() {
        runBlocking {
            val charactersRepository =
                CharactersInMemoryRepository().withState(CharactersState(listOf(basicCharacter), listOf()))
            val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)

            assertEquals(listOf(basicCharacter), charactersService.get(Game.WOW))
        }
    }
}