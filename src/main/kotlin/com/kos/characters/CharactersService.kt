package com.kos.characters

import arrow.core.Either
import com.kos.characters.repository.CharactersRepository
import com.kos.common.split

data class CharactersService(private val charactersRepository: CharactersRepository) {
    suspend fun createAndReturnIds(characters: List<CharacterRequest>): List<Long> {
        fun splitExistentAndNew(charactersRequest: List<CharacterRequest>, currentCharacters: List<Character>): List<Either<Character, CharacterRequest>> {
            return charactersRequest.fold(emptyList()) { acc, character ->
                when (val maybeCharacter =
                    currentCharacters.find { it.realm == character.realm && it.name == character.name && it.region == character.region }) {
                    null -> acc + Either.Right(character)
                    else -> acc + Either.Left(maybeCharacter)
                }
            }
        }

        val currentCharacters = charactersRepository.get()
        val existentAndNew = splitExistentAndNew(characters, currentCharacters).split()

        return charactersRepository.insert(existentAndNew.second).map { it.id } + existentAndNew.first.map { it.id }
    }

    suspend fun get(id: Long): Character? = charactersRepository.get(id)
}