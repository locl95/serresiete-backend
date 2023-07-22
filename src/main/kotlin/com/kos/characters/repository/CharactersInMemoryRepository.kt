package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest

class CharactersInMemoryRepository(initialState: List<Character> = listOf()): CharactersRepository {
    val characters: MutableList<Character> = mutableListOf()

    init {
        characters.addAll(initialState)
    }

    private fun nextVersion(): Long {
        return if (characters.isEmpty()) 1
        else characters.map {it.id}.maxBy { it } + 1
    }

    override fun insert(character: CharacterRequest) =
        when (val maybeChar = characters.find { character.same(it)  }) {
            null -> {
                val element = character.toCharacter(nextVersion())
                characters.add(element)
                element
            }
            else -> maybeChar
        }

    override fun get(id: Long): Character? = characters.find {it.id == id}

}