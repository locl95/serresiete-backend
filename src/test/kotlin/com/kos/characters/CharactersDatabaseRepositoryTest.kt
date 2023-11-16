package com.kos.characters

import com.kos.characters.CharactersTestHelper.basicCharacter
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersDatabaseRepositoryTest: CharactersRepositoryTest {
    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }

    @Test
    override fun ICanInsertCharacters() {
        val repository = CharactersDatabaseRepository()
        val request = CharacterRequest(basicCharacter.name, basicCharacter.region, basicCharacter.realm)
        val expected = listOf(basicCharacter)

        runBlocking { assertEquals(expected, repository.insert(listOf(request))) }
    }
}