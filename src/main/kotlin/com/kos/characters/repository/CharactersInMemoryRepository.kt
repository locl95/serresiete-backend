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
        val allIds = wowCharacters.map { it.id } + lolCharacters.map { it.id }
        return if (allIds.isEmpty()) 1
        else allIds.maxBy { it } + 1
    }

    override suspend fun insert(
        characters: List<CharacterInsertRequest>,
        game: Game
    ): Either<InsertCharacterError, List<Character>> {
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
                                return Either.Left(InsertCharacterError("Error inserting character $it"))
                            }
                            val character = it.toCharacter(nextId())
                            this.wowCharacters.add(character)
                            acc + character
                        }

                        is LolCharacterEnrichedRequest -> {
                            this.wowCharacters.clear()
                            this.wowCharacters.addAll(wowInitialCharacters)
                            return Either.Left(InsertCharacterError("Error inserting character $it"))
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
                            return Either.Left(InsertCharacterError("Error inserting chracter $it"))
                        }

                        is LolCharacterEnrichedRequest -> {
                            if (this.lolCharacters.any { character -> it.same(character) }) {
                                this.lolCharacters.clear()
                                this.lolCharacters.addAll(lolInitialCharacters)
                                return Either.Left(InsertCharacterError("Error inserting chracter $it"))
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