package com.kos.characters

import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicLolCharacterEnrichedRequest
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter2
import com.kos.characters.CharactersTestHelper.basicWowRequest
import com.kos.characters.CharactersTestHelper.basicWowRequest2
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersRepository
import com.kos.characters.repository.CharactersState
import com.kos.common.DatabaseFactory
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import kotlin.test.*

abstract class CharactersRepositoryTestCommon {

    abstract val repository: CharactersRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty repository i can insert wow characters`() {
        runBlocking {
            val expected = listOf(basicWowCharacter)
            repository.insert(listOf(basicWowRequest), Game.WOW).fold({ fail() }) { assertEquals(expected, it) }
        }
    }

    @Test
    fun `given an empty repository i can insert lol characters`() {
        runBlocking {
            val expected = listOf(basicLolCharacter)
            repository.insert(listOf(basicLolCharacterEnrichedRequest), Game.LOL)
                .fold({ fail() }) { assertEquals(expected, it) }
        }
    }

    @Test
    fun `given an empty repository inserting a wow character that already exists fails`() {
        runBlocking {
            val character = WowCharacterRequest(
                basicWowCharacter.name,
                basicWowCharacter.region,
                basicWowCharacter.realm
            )

            val initialState = repository.state()
            assertEquals(emptyCharactersState, initialState)
            assertTrue(repository.insert(listOf(character, character), Game.WOW).isLeft())

            val finalState = repository.state()
            assertEquals(emptyCharactersState, finalState)
        }
    }

    @Test
    fun `given a repository that includes a wow character, adding the same one fails`() {
        runBlocking {
            val repo =
                repository.withState(CharactersState(listOf(basicWowCharacter, basicWowCharacter2), listOf()))
            assertTrue(repo.insert(listOf(basicWowRequest), Game.WOW).isLeft())
            assertEquals(
                CharactersState(listOf(basicWowCharacter, basicWowCharacter2), listOf()),
                repository.state()
            )
        }
    }

    @Test
    fun `given a repository with characters of multiple types, I can retrieve them one by one`() {
        runBlocking {
            val repo = repository.withState(CharactersState(listOf(basicWowCharacter), listOf(basicLolCharacter)))
            assertEquals(basicWowCharacter, repo.get(basicWowCharacter.id, Game.WOW))
            assertEquals(basicLolCharacter, repo.get(basicLolCharacter.id, Game.LOL))
        }
    }

    @Test
    fun `given a repository with characters of multiple types, I can retrieve all of them`() {
        runBlocking {
            val repo = repository.withState(
                CharactersState(
                    listOf(basicWowCharacter, basicWowCharacter2),
                    listOf(basicLolCharacter)
                )
            )
            assertEquals(listOf(basicWowCharacter, basicWowCharacter2), repo.get(Game.WOW))
            assertEquals(listOf(basicLolCharacter), repo.get(Game.LOL))
        }
    }

    @Test
    fun `given an empty repository, I can't insert characters when game does not match`() {
        runBlocking {
            assertTrue(repository.insert(listOf(basicLolCharacterEnrichedRequest), Game.WOW).isLeft())
            assertTrue(repository.insert(listOf(basicWowRequest), Game.LOL).isLeft())
            assertEquals(emptyCharactersState, repository.state())
        }
    }

    @Test
    fun `given a repository with wow characters, I can insert more`() {
        runBlocking {
            val repositoryWithState = repository.withState(CharactersState(listOf(basicWowCharacter), listOf()))
            val inserted = repositoryWithState.insert(listOf(basicWowRequest2), Game.WOW)
            inserted
                .onRight { characters -> assertEquals(listOf<Long>(2), characters.map { it.id }) }
                .onLeft { fail(it.message) }
        }
    }

    @Test
    fun `given a repository with lol characters, I can insert more`() {
        runBlocking {
            val repositoryWithState = repository.withState(CharactersState(listOf(), listOf(basicLolCharacter)))
            val request = basicLolCharacterEnrichedRequest.copy(puuid = "different-puuid", summonerId = "different-summoner-id")
            val inserted = repositoryWithState.insert(listOf(request), Game.LOL)
            inserted
                .onRight { characters -> assertEquals(listOf<Long>(2), characters.map { it.id }) }
                .onLeft { fail(it.message) }
        }
    }

    @Test
    fun `i can insert a lol character with a tag longer than 3 characters`() {
        runBlocking {
            val request = basicLolCharacterEnrichedRequest.copy(tag= "12345")
            val inserted = repository.insert(listOf(request), Game.LOL)
            inserted
                .onRight { characters -> assertEquals(listOf<Long>(1), characters.map { it.id }) }
                .onLeft { fail(it.message) }
        }
    }


}

class CharactersInMemoryRepositoryTest : CharactersRepositoryTestCommon() {
    override val repository = CharactersInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class CharactersDatabaseRepositoryTest : CharactersRepositoryTestCommon() {
    override val repository = CharactersDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
