package com.kos.characters

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.kos.characters.repository.CharactersRepository
import com.kos.common.*
import com.kos.datacache.DataCache
import com.kos.httpclients.domain.Data
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.encodeToString
import java.time.OffsetDateTime

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

    suspend fun updateLolCharacters(characters: List<LolCharacter>): List<ControllerError> =
        coroutineScope {
            val errorsChannel = Channel<ControllerError>()
            val dataChannel = Channel<Pair<LolCharacterEnrichedRequest, Long>>()
            val errorsList = mutableListOf<ControllerError>()

            val errorsCollector = launch {
                errorsChannel.consumeAsFlow().collect { error ->
                    logger.error(error.toString())
                    errorsList.add(error)
                }
            }

            val dataCollector = launch {
                dataChannel.consumeAsFlow()
                    .buffer(50)
                    .collect { characterWithId ->
                        charactersRepository.update(characterWithId.second, characterWithId.first, Game.LOL)
                        logger.info("updated character ${characterWithId.second}")
                    }
            }

            characters.asFlow()
                .buffer(40)
                .collect { lolCharacter ->
                    val result = retrieveUpdatedLolCharacter(lolCharacter)
                    result.fold(
                        ifLeft = { error -> errorsChannel.send(error) },
                        ifRight = { dataChannel.send(Pair(it, lolCharacter.id)) }
                    )
                }
            dataChannel.close()
            errorsChannel.close()

            errorsCollector.join()
            dataCollector.join()

            logger.info("Finished Updating Lol characters")
            errorsList
        }

    private suspend fun retrieveUpdatedLolCharacter(lolCharacter: LolCharacter): Either<HttpError, LolCharacterEnrichedRequest> =
        either {
            val summoner = riotClient.getSummonerByPuuid(lolCharacter.puuid).bind()
            val account = riotClient.getAccountByPUUID(lolCharacter.puuid).bind()
            LolCharacterEnrichedRequest(
                name = account.gameName,
                tag = account.tagLine,
                puuid = lolCharacter.puuid,
                summonerIconId = summoner.profileIconId,
                summonerId = summoner.id,
                summonerLevel = summoner.summonerLevel
            )
        }


    suspend fun get(id: Long, game: Game): Character? = charactersRepository.get(id, game)
    suspend fun get(game: Game): List<Character> = charactersRepository.get(game)
}