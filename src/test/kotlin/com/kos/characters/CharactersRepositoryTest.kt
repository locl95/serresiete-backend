package com.kos.characters

import arrow.core.right
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import kotlin.test.*

abstract class CharactersRepositoryTestCommon {

    abstract val repository: CharactersRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty repository i can insert characters`() {
        runBlocking {
            val request = CharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )
            val expected = listOf(CharactersTestHelper.basicCharacter)
            repository.insert(listOf(request)).fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `given an empty repository inserting a character that already exists fails`() {
        runBlocking {
            val character = CharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )

            val initialState = repository.state()
            assertEquals(listOf(), initialState)
            assertTrue(repository.insert(listOf(character, character)).isLeft())

            val finalState = repository.state()
            assertEquals(listOf(), finalState)
        }
    }

    @Test
    fun `given a repository that includes a character, adding the same one fails`() {
        runBlocking {
            val character = CharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )
            val character2 = CharacterRequest(
                CharactersTestHelper.basicCharacter2.name,
                CharactersTestHelper.basicCharacter2.region,
                CharactersTestHelper.basicCharacter2.realm
            )

            assertTrue(repository.insert(listOf(character, character2)).isRight())
            val initialState = repository.state()
            assertEquals(listOf(CharactersTestHelper.basicCharacter, CharactersTestHelper.basicCharacter2), initialState)

            assertTrue(repository.insert(listOf(character)).isLeft())

            val finalState = repository.state()
            assertEquals(listOf(CharactersTestHelper.basicCharacter, CharactersTestHelper.basicCharacter2), finalState)


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
