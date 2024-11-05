package com.kos.characters.repository

import arrow.core.Either
import com.kos.characters.*
import com.kos.common.InsertError
import com.kos.views.Game
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class CharactersDatabaseRepository(private val db: Database) : CharactersRepository {

    override suspend fun withState(initialState: CharactersState): CharactersDatabaseRepository {
        newSuspendedTransaction(Dispatchers.IO, db) {
            WowCharacters.batchInsert(initialState.wowCharacters) {
                this[WowCharacters.id] = it.id
                this[WowCharacters.name] = it.name
                this[WowCharacters.region] = it.region
                this[WowCharacters.realm] = it.realm
            }
            LolCharacters.batchInsert(initialState.lolCharacters) {
                this[LolCharacters.id] = it.id
                this[LolCharacters.name] = it.name
                this[LolCharacters.tag] = it.tag
                this[LolCharacters.puuid] = it.puuid
                this[LolCharacters.summonerIcon] = it.summonerIcon
                this[LolCharacters.summonerId] = it.summonerId
                this[LolCharacters.summonerLevel] = it.summonerLevel
            }
        }
        //This needs to be done to consume serial ids. Could be done in a different way but I don't dislike it.
        initialState.lolCharacters.forEach { _ -> selectNextId() }
        initialState.wowCharacters.forEach { _ -> selectNextId() }
        return this
    }

    object WowCharacters : Table("wow_characters") {
        val id = long("id")
        val name = text("name")
        val realm = text("realm")
        val region = text("region")

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToWowCharacter(row: ResultRow) = WowCharacter(
        row[WowCharacters.id],
        row[WowCharacters.name],
        row[WowCharacters.region],
        row[WowCharacters.realm]
    )

    object LolCharacters : Table("lol_characters") {
        val id = long("id")
        val name = text("name")
        val tag = text("tag")
        val puuid = text("puuid")
        val summonerIcon = integer("summoner_icon")
        val summonerId = text("summoner_id")
        val summonerLevel = integer("summoner_level")

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToLolCharacter(row: ResultRow) = LolCharacter(
        row[LolCharacters.id],
        row[LolCharacters.name],
        row[LolCharacters.tag],
        row[LolCharacters.puuid],
        row[LolCharacters.summonerIcon],
        row[LolCharacters.summonerId],
        row[LolCharacters.summonerLevel]
    )

    override suspend fun insert(
        characters: List<CharacterInsertRequest>,
        game: Game
    ): Either<InsertError, List<Character>> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            val charsToInsert: List<Character> = characters.map {
                when (it) {
                    is WowCharacterRequest -> WowCharacter(selectNextId(), it.name, it.region, it.realm)
                    is LolCharacterEnrichedRequest -> LolCharacter(
                        selectNextId(),
                        it.name,
                        it.tag,
                        it.puuid,
                        it.summonerIconId,
                        it.summonerId,
                        it.summonerLevel
                    )
                }
            }
            transaction {
                try {
                    val insertedCharacters = when (game) {
                        Game.WOW -> WowCharacters.batchInsert(charsToInsert) {
                            when (it) {
                                is WowCharacter -> {
                                    this[WowCharacters.id] = it.id
                                    this[WowCharacters.name] = it.name
                                    this[WowCharacters.region] = it.region
                                    this[WowCharacters.realm] = it.realm
                                }

                                is LolCharacter -> throw IllegalArgumentException()
                            }
                        }.map { resultRowToWowCharacter(it) }

                        Game.LOL -> LolCharacters.batchInsert(charsToInsert) {
                            when (it) {
                                is WowCharacter -> throw IllegalArgumentException()
                                is LolCharacter -> {
                                    this[LolCharacters.id] = it.id
                                    this[LolCharacters.name] = it.name
                                    this[LolCharacters.tag] = it.tag
                                    this[LolCharacters.puuid] = it.puuid
                                    this[LolCharacters.summonerIcon] = it.summonerIcon
                                    this[LolCharacters.summonerId] = it.summonerId
                                    this[LolCharacters.summonerLevel] = it.summonerLevel
                                }
                            }
                        }.map { resultRowToLolCharacter(it) }
                    }
                    Either.Right(insertedCharacters)
                } catch (e: SQLException) {
                    rollback() //TODO: I don't understand why rollback is not provided by dbQuery.
                    Either.Left(InsertError(e.message ?: e.stackTraceToString()))
                } catch (e: IllegalArgumentException) {
                    rollback() //TODO: I don't understand why rollback is not provided by dbQuery.
                    Either.Left(InsertError(e.message ?: e.stackTraceToString()))
                }
            }
        }
    }

    override suspend fun update(
        id: Long,
        character: CharacterInsertRequest,
        game: Game
    ): Either<InsertCharacterError, Int> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            when (game) {
                Game.LOL -> {
                    when (character) {
                        is LolCharacterEnrichedRequest -> {
                            Either.Right(LolCharacters.update({ LolCharacters.id eq id }) {
                                it[name] = character.name
                                it[tag] = character.tag
                                it[puuid] = character.puuid
                                it[summonerIcon] = character.summonerIconId
                                it[summonerId] = character.summonerId
                                it[summonerLevel] = character.summonerLevel
                            })
                        }
                        else -> Either.Left(InsertCharacterError("problem updating $id: $character for $game"))
                    }
                }
                Game.WOW -> when (character) {
                    is WowCharacterRequest -> {
                        Either.Right(WowCharacters.update({WowCharacters.id eq id}) {
                            it[name] = character.name
                            it[region] = character.region
                            it[realm] = character.realm
                        })
                    }
                    else -> Either.Left(InsertCharacterError("problem updating $id: $character for $game"))
                }
            }
        }
    }

    override suspend fun get(id: Long, game: Game): Character? {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            when (game) {
                Game.WOW -> WowCharacters.select { WowCharacters.id.eq(id) }.singleOrNull()?.let {
                    resultRowToWowCharacter(it)
                }

                Game.LOL -> LolCharacters.select { LolCharacters.id.eq(id) }.singleOrNull()?.let {
                    resultRowToLolCharacter(it)
                }
            }
        }
    }

    override suspend fun get(game: Game): List<Character> =
        newSuspendedTransaction(Dispatchers.IO, db) {
            when (game) {
                Game.WOW -> WowCharacters.selectAll().map { resultRowToWowCharacter(it) }
                Game.LOL -> LolCharacters.selectAll().map { resultRowToLolCharacter(it) }
            }
        }


    override suspend fun state(): CharactersState {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            CharactersState(
                WowCharacters.selectAll().map { resultRowToWowCharacter(it) },
                LolCharacters.selectAll().map { resultRowToLolCharacter(it) }
            )
        }
    }

    private suspend fun selectNextId(): Long =
        newSuspendedTransaction(Dispatchers.IO, db) {
            TransactionManager.current().exec("""select nextval('characters_ids') as id""") { rs ->
                if (rs.next()) rs.getLong("id")
                else -1
            }
        } ?: -1
}