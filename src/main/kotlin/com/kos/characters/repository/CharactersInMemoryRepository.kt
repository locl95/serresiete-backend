package com.kos.characters.repository

import arrow.core.Either
import com.kos.characters.*
import com.kos.common.InMemoryRepository
import com.kos.common.InsertError
import com.kos.views.Game

class CharactersInMemoryRepository : CharactersRepository, InMemoryRepository {
    private val wowCharacters: MutableList<WowCharacter> = mutableListOf()
    private val lolCharacters: MutableList<LolCharacter> = mutableListOf()

    private fun nextId(): Long {
        val allIds = wowCharacters.map { it.id } + lolCharacters.map { it.id }
        return if (allIds.isEmpty()) 1
        else allIds.maxBy { it } + 1
    }

    override suspend fun insert(
        characters: List<CharacterInsertRequest>,
        game: Game
    ): Either<InsertError, List<Character>> {
        val wowInitialCharacters = this.wowCharacters.toList()
        val lolInitialCharacters = this.lolCharacters.toList()
        when (game) {
            Game.WOW -> {
                val inserted = characters.fold(listOf<Character>()) { acc, it ->
                    when (it) {
                        is WowCharacterRequest -> {
                            if (this.wowCharacters.any { character -> it.same(character) }) {
                                this.wowCharacters.clear()
                                this.wowCharacters.addAll(wowInitialCharacters)
                                return Either.Left(InsertError("Error inserting character $it"))
                            }
                            val character = it.toCharacter(nextId())
                            this.wowCharacters.add(character)
                            acc + character
                        }

                        is LolCharacterEnrichedRequest -> {
                            this.wowCharacters.clear()
                            this.wowCharacters.addAll(wowInitialCharacters)
                            return Either.Left(InsertError("Error inserting character $it"))
                        }
                    }
                }
                return Either.Right(inserted)
            }

            Game.LOL -> {
                val inserted = characters.fold(listOf<Character>()) { acc, it ->
                    when (it) {
                        is WowCharacterRequest -> {
                            this.lolCharacters.clear()
                            this.lolCharacters.addAll(lolInitialCharacters)
                            return Either.Left(InsertError("Error inserting chracter $it"))
                        }

                        is LolCharacterEnrichedRequest -> {
                            if (this.lolCharacters.any { character -> it.same(character) }) {
                                this.lolCharacters.clear()
                                this.lolCharacters.addAll(lolInitialCharacters)
                                return Either.Left(InsertError("Error inserting chracter $it"))
                            }
                            val character = it.toCharacter(nextId())
                            this.lolCharacters.add(character)
                            acc + character
                        }
                    }
                }
                return Either.Right(inserted)
            }
        }
    }

    override suspend fun update(
        id: Long,
        character: CharacterInsertRequest,
        game: Game
    ): Either<InsertError, Int> {
        return when(game) {
            Game.LOL -> when(character) {
                is LolCharacterEnrichedRequest -> {
                    val index = lolCharacters.indexOfFirst { it.id == id }
                    lolCharacters.removeAt(index)
                    val c = LolCharacter(
                        id,
                        character.name,
                        character.tag,
                        character.puuid,
                        character.summonerIconId,
                        character.summonerId,
                        character.summonerLevel
                    )
                    lolCharacters.add(index, c)
                    Either.Right(1)
                }
                else -> Either.Left(InsertError("error updating $id $character for $game"))
            }
            Game.WOW -> when(character) {
                is WowCharacterRequest -> {
                    val index = wowCharacters.indexOfFirst { it.id == id }
                    wowCharacters.removeAt(index)
                    val c = WowCharacter(
                        id,
                        character.name,
                        character.region,
                        character.realm
                    )
                    wowCharacters.add(index, c)
                    Either.Right(1)
                }
                else -> Either.Left(InsertError("error updating $id $character for $game"))
            }
        }
    }

    override suspend fun get(id: Long, game: Game): Character? =
        when (game) {
            Game.WOW -> wowCharacters.find { it.id == id }
            Game.LOL -> lolCharacters.find { it.id == id }
        }

    override suspend fun get(game: Game): List<Character> =
        when (game) {
            Game.WOW -> wowCharacters
            Game.LOL -> lolCharacters
        }

    override suspend fun state(): CharactersState {
        return CharactersState(wowCharacters, lolCharacters)
    }

    override suspend fun withState(initialState: CharactersState): CharactersInMemoryRepository {
        wowCharacters.addAll(initialState.wowCharacters)
        lolCharacters.addAll(initialState.lolCharacters)
        return this
    }

    override fun clear() {
        wowCharacters.clear()
        lolCharacters.clear()
    }

}