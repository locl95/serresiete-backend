package com.kos.characters

import com.kos.characters.repository.CharactersRepository

data class CharactersService(private val charactersRepository: CharactersRepository) {
    suspend fun create(characters: List<CharacterRequest>): List<Long> =
        characters.fold<CharacterRequest, List<Character?>>(listOf()) { acc, req ->
            acc.plus(charactersRepository.insert(req))
        }.mapNotNull { it?.id }

    suspend fun get(id: Long): Character? = charactersRepository.get(id)
}