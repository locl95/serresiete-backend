package com.kos.characters

import arrow.core.Either
import arrow.core.flatMap
import com.kos.characters.repository.CharactersRepository
import com.kos.common.InsertCharacterError
import com.kos.common.WithLogger
import com.kos.common.collect
import com.kos.common.split
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class CharactersService(
    private val charactersRepository: CharactersRepository,
    private val raiderioClient: RaiderIoClient,
    private val riotClient: RiotClient
) : WithLogger("CharactersService") {
    suspend fun createAndReturnIds(
        characters: List<CharacterCreateRequest>,
        game: Game
    ): Either<InsertCharacterError, List<Long>> {
        fun splitExistentAndNew(
            charactersRequest: List<CharacterCreateRequest>,
            currentCharacters: List<Character>
        ): List<Either<Character, CharacterCreateRequest>> {
            return charactersRequest.fold(emptyList()) { acc, character ->
                when (val maybeCharacter =
                    currentCharacters.find { character.same(it) }) {
                    null -> acc + Either.Right(character)
                    else -> acc + Either.Left(maybeCharacter)
                }
            }
        }

        val currentCharacters = charactersRepository.get(game)
        val existentAndNew = splitExistentAndNew(characters, currentCharacters).split()

        existentAndNew.second.forEach { logger.info("Character new found: $it") }

        val newThatExist = when (game) {
            Game.WOW -> {
                coroutineScope {
                    existentAndNew.second.map { initialRequest ->
                        async {
                            initialRequest as WowCharacterRequest
                            initialRequest to raiderioClient.exists(initialRequest)
                        }
                    }
                        .awaitAll()
                }.collect({ it.second }) { it.first }
            }

            Game.LOL -> coroutineScope {
                val x = existentAndNew.second.map { initialRequest ->
                    async {
                        initialRequest as LolCharacterRequest
                        riotClient.getPUUIDByRiotId(initialRequest.name, initialRequest.tag).flatMap {
                            riotClient.getSummonerByPuuid(it.puuid).map { summonerResponse ->
                                LolCharacterEnrichedRequest(
                                    initialRequest.name,
                                    initialRequest.tag,
                                    summonerResponse.puuid,
                                    summonerResponse.profileIconId,
                                    summonerResponse.id,
                                    summonerResponse.summonerLevel
                                )
                            }
                        }
                    }
                }.awaitAll().split()
                x.first.forEach { logger.info(it.error()) }
                x.second.filter { charactersToInsert ->
                    !currentCharacters.any { charactersToInsert.same(it) }
                }
            }
        }


        return charactersRepository.insert(newThatExist, game)
            .map { list -> list.map { it.id } + existentAndNew.first.map { it.id } }
    }

    suspend fun get(id: Long, game: Game): Character? = charactersRepository.get(id, game)
    suspend fun get(game: Game): List<Character> = charactersRepository.get(game)
}