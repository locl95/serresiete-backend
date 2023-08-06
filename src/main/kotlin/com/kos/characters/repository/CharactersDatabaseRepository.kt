package com.kos.characters.repository

import com.kos.characters.Character
import com.kos.characters.CharacterRequest
import com.kos.common.DatabaseFactory.dbQuery
import com.kos.views.SimpleView
import com.kos.views.repository.ViewsDatabaseRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager

class CharactersDatabaseRepository : CharactersRepository {

    suspend fun withState(initialState: List<Character>): CharactersDatabaseRepository {
        dbQuery {
            Characters.batchInsert(initialState) {
                this[Characters.id] = it.id
                this[Characters.name] = it.name
                this[Characters.realm] = it.realm
                this[Characters.region] = it.region
            }
        }
        return this
    }

    object Characters : Table() {
        val id = long("id")
        val name = text("name")
        val realm = text("realm")
        val region = text("region")

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToCharacter(row: ResultRow) = Character(
        row[Characters.id],
        row[Characters.name],
        row[Characters.region],
        row[Characters.realm]
    )

    override suspend fun insert(character: CharacterRequest): Character? {
        val id = selectNextId()
        /* https://github.com/JetBrains/Exposed/issues/167 */
        return dbQuery {
            Characters.insert {
                it[Characters.id] = id
                it[name] = character.name
                it[realm] = character.realm
                it[region] = character.region
            }.resultedValues?.singleOrNull()?.let { resultRowToCharacter(it) }
        }
    }

    override suspend fun get(id: Long): Character? {
        return dbQuery {
            Characters.select { Characters.id.eq(id) }.singleOrNull()?.let {
                resultRowToCharacter(it)
            }
        }
    }

    override suspend fun state(): List<Character> {
        return dbQuery {
            Characters.selectAll().map { resultRowToCharacter(it) }
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