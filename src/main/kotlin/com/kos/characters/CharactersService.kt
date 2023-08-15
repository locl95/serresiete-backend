package com.kos.characters

import com.kos.characters.repository.CharactersRepository

data class CharactersService(private val charactersRepository: CharactersRepository) {
    suspend fun create(characters: List<CharacterRequest>): List<Long> {
        val currentCharacters = charactersRepository.get()
        return charactersRepository.insert(characters.minus(currentCharacters.map {it.toCharacterRequest()}.toSet())).map { it.id }
    }

    suspend fun get(id: Long): Character? = charactersRepository.get(id)
}