package com.kos.characters

import arrow.core.Either
import com.kos.characters.repository.CharactersRepository
import com.kos.common.InsertCharacterError
import com.kos.common.WithLogger
import com.kos.common.collect
import com.kos.common.split
import com.kos.raiderio.RaiderIoClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class CharactersService(
    private val charactersRepository: CharactersRepository,
    private val raiderioClient: RaiderIoClient
): WithLogger("CharactersService") {
    suspend fun createAndReturnIds(characters: List<CharacterRequest>): Either<InsertCharacterError, List<Long>> {
        fun splitExistentAndNew(
            charactersRequest: List<CharacterRequest>,
            currentCharacters: List<Character>
        ): List<Either<Character, CharacterRequest>> {
            return charactersRequest.fold(emptyList()) { acc, character ->
                when (val maybeCharacter =
                    currentCharacters.find { character.same(it) }) {
                    null -> acc + Either.Right(character)
                    else -> acc + Either.Left(maybeCharacter)
                }
            }
        }

        val currentCharacters = charactersRepository.get()
        val existentAndNew = splitExistentAndNew(characters, currentCharacters).split()

        existentAndNew.second.forEach { logger.info("Character new found: $it") }
        val newThatExist = coroutineScope {
            existentAndNew.second.map { async { it to raiderioClient.exists(it) } }.awaitAll()
        }.collect({ it.second }) { it.first }

        return charactersRepository.insert(newThatExist)
            .map { list -> list.map { it.id } + existentAndNew.first.map { it.id } }
    }

    suspend fun get(id: Long): Character? = charactersRepository.get(id)
    suspend fun get(): List<Character> = charactersRepository.get()
}