package com.kos.characters

import kotlin.test.Test

interface CharactersRepositoryTest {
    @Test
    fun ICanInsertCharacters()

    @Test
    fun InsertingCharactersThatAlreadyExistDoesNothing()
}