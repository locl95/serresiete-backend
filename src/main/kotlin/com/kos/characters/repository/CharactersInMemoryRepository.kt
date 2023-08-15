package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest

class CharactersInMemoryRepository(initialState: List<Character> = listOf()): CharactersRepository {
    val characters: MutableList<Character> = mutableListOf()

    init {
        characters.addAll(initialState)
    }

    private fun nextId(): Long {
        return if (characters.isEmpty()) 1
        else characters.map {it.id}.maxBy { it } + 1
    }
    override suspend fun insert(characters: List<CharacterRequest>): List<Character> {
        characters.forEach {
            this.characters.add(it.toCharacter(nextId()))
        }
        return this.characters
    }

    override suspend fun get(id: Long): Character? = characters.find {it.id == id}
    override suspend fun get(): List<Character> = characters

    override suspend fun state(): List<Character> {
        return characters
    }

}