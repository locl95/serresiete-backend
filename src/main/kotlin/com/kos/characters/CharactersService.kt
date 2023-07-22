package com.kos.characters

import com.kos.characters.repository.CharactersRepository

data class CharactersService(private val charactersRepository: CharactersRepository) {
    fun create(characters: List<CharacterRequest>): List<Long> =
        characters.fold<CharacterRequest, List<Character>>(listOf()) { acc, req ->
            acc.plus(charactersRepository.insert(req))
        }.map { it.id }

    fun get(id: Long): Character? = charactersRepository.get(id)
}