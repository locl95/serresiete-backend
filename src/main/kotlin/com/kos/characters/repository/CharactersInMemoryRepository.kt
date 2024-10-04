package com.kos.characters.repository

import arrow.core.Either
import com.kos.characters.*
import com.kos.common.InMemoryRepository
import com.kos.common.InsertCharacterError
import com.kos.views.Game

class CharactersInMemoryRepository : CharactersRepository, InMemoryRepository {
    private val wowCharacters: MutableList<WowCharacter> = mutableListOf()
    private val lolCharacters: MutableList<LolCharacter> = mutableListOf()

    private fun nextId(): Long {
        return if (wowCharacters.isEmpty()) 1
        else wowCharacters.map { it.id }.maxBy { it } + 1
    }

    override suspend fun insert(
        characters: List<CharacterInsertRequest>,
        game: Game
    ): Either<InsertCharacterError, List<Character>> {
        val wowInitialCharacters = this.wowCharacters.toList()
        val lolInitialCharacters = this.lolCharacters.toList()
        when (game) {
            Game.WOW -> {
                characters.forEach {
                    when (it) {
                        is WowCharacterRequest -> {
                            if (this.wowCharacters.any { character -> it.same(character) }) {
                                this.wowCharacters.clear()
                                this.wowCharacters.addAll(wowInitialCharacters)
                                return Either.Left(InsertCharacterError("Error inserting character $it"))
                            }
                            this.wowCharacters.add(it.toCharacter(nextId()))
                        }

                        is LolCharacterEnrichedRequest -> {
                            this.wowCharacters.clear()
                            this.wowCharacters.addAll(wowInitialCharacters)
                            return Either.Left(InsertCharacterError("Error inserting character $it"))
                        }
                    }
                }
                return Either.Right(this.wowCharacters)
            }

            Game.LOL -> {
                characters.forEach {
                    when (it) {
                        is WowCharacterRequest -> {
                            this.lolCharacters.clear()
                            this.lolCharacters.addAll(lolInitialCharacters)
                            return Either.Left(InsertCharacterError("Error inserting chracter $it"))
                        }

                        is LolCharacterEnrichedRequest -> {
                            println(this.lolCharacters)
                            if (this.lolCharacters.any { character -> it.same(character) }) {
                                this.lolCharacters.clear()
                                this.lolCharacters.addAll(lolInitialCharacters)
                                return Either.Left(InsertCharacterError("Error inserting chracter $it"))
                            }
                            this.lolCharacters.add(it.toCharacter(nextId()))
                        }
                    }
                }
                return Either.Right(this.lolCharacters)
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