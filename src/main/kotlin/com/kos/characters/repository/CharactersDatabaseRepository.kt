package com.kos.characters.repository

import arrow.core.Either
import com.kos.characters.*
import com.kos.common.DatabaseFactory.dbQuery
import com.kos.common.InsertCharacterError
import com.kos.views.Game
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class CharactersDatabaseRepository : CharactersRepository {

    override suspend fun withState(initialState: CharactersState): CharactersDatabaseRepository {
        dbQuery {
            WowCharacters.batchInsert(initialState.wowCharacters) {
                this[WowCharacters.id] = it.id
                this[WowCharacters.name] = it.name
                this[WowCharacters.realm] = it.realm
                this[WowCharacters.region] = it.region
            }
            LolCharacters.batchInsert(initialState.lolCharacters) {
                this[LolCharacters.id] = it.id
                this[WowCharacters.name] = it.name
                this[LolCharacters.tag] = it.tag
                this[LolCharacters.puuid] = it.puuid
                this[LolCharacters.summonerIcon] = it.summonerIcon
                this[LolCharacters.summonerId] = it.summonerId
                this[LolCharacters.summonerLevel] = it.summonerLevel
            }
        }
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
    ): Either<InsertCharacterError, List<Character>> {
        return dbQuery {
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
                    Either.Left(InsertCharacterError(e.message ?: e.stackTraceToString()))
                } catch (e: IllegalArgumentException) {
                    rollback() //TODO: I don't understand why rollback is not provided by dbQuery.
                    Either.Left(InsertCharacterError(e.message ?: e.stackTraceToString()))
                }
            }
        }
    }

    override suspend fun get(id: Long, game: Game): Character? {
        return dbQuery {
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
        dbQuery {
            when (game) {
                Game.WOW -> WowCharacters.selectAll().map { resultRowToWowCharacter(it) }
                Game.LOL -> LolCharacters.selectAll().map { resultRowToLolCharacter(it) }
            }
        }


    override suspend fun state(): CharactersState {
        return dbQuery {
           CharactersState(
                WowCharacters.selectAll().map { resultRowToWowCharacter(it) },
                LolCharacters.selectAll().map { resultRowToLolCharacter(it) }
            )
        }
    }

    suspend fun selectNextId(): Long =
        dbQuery {
            TransactionManager.current().exec("""select nextval('characters_ids') as id""") { rs ->
                if (rs.next()) rs.getLong("id")
                else -1
            }
        } ?: -1
}