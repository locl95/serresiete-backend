package com.kos.characters

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.kos.characters.repository.CharactersRepository
import com.kos.common.*
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

    suspend fun updateLolCharacters(characters: List<LolCharacter>): List<Either<ControllerError, Int>> {
        return coroutineScope {
            val semaphore = Semaphore(40)
            characters.map { lolCharacter ->
                async {
                    semaphore.withPermit {
                        either {
                            val summoner = riotClient.getSummonerByPuuid(lolCharacter.puuid).bind()
                            val account = riotClient.getAccountByPUUID(lolCharacter.puuid).bind()
                            charactersRepository.update(
                                id = lolCharacter.id,
                                character = LolCharacterEnrichedRequest(
                                    name = account.gameName,
                                    tag = account.tagLine,
                                    puuid = lolCharacter.puuid,
                                    summonerIconId = summoner.profileIconId,
                                    summonerId = summoner.id,
                                    summonerLevel = summoner.summonerLevel
                                ),
                                Game.LOL
                            ).bind()
                        }
                    }
                }
            }.awaitAll()
        }
    }

    suspend fun get(id: Long, game: Game): Character? = charactersRepository.get(id, game)
    suspend fun get(game: Game): List<Character> = charactersRepository.get(game)
}