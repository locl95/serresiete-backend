package com.kos.views.repository

import com.kos.common.DatabaseFactory.dbQuery
import com.kos.views.SimpleView
import com.kos.views.ViewDeleted
import com.kos.views.ViewModified
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ViewsDatabaseRepository : ViewsRepository {

    override suspend fun withState(initialState: List<SimpleView>): ViewsDatabaseRepository {
        dbQuery {
            Views.batchInsert(initialState) {
                this[Views.id] = it.id
                this[Views.name] = it.name
                this[Views.owner] = it.owner
            }
            initialState.forEach { sv ->
                CharactersView.batchInsert(sv.characterIds) {
                    this[CharactersView.viewId] = sv.id
                    this[CharactersView.characterId] = it
                }
            }
        }
        return this
    }

    object Views : Table() {
        val id = varchar("id", 48)
        val name = varchar("name", 128)
        val owner = varchar("owner", 48)

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToSimpleView(row: ResultRow): SimpleView {
        return SimpleView(
            row[Views.id],
            row[Views.name],
            row[Views.owner],
            CharactersView.select { CharactersView.viewId.eq(row[Views.id]) }
                .map { resultRowToCharacterView(it).first }
        )
    }

    object CharactersView : Table() {
        val characterId = long("character_id")
        val viewId = varchar("view_id", 48)

        override val tableName = "characters_view"
    }

    private fun resultRowToCharacterView(row: ResultRow): Pair<Long, String> = Pair(
        row[CharactersView.characterId],
        row[CharactersView.viewId],
    )

    override suspend fun getOwnViews(owner: String): List<SimpleView> {
        return dbQuery {
            Views.select { Views.owner.eq(owner) }.map { resultRowToSimpleView(it) }
        }
    }

    override suspend fun get(id: String): SimpleView? {
        return dbQuery {
            Views.select { Views.id.eq(id) }.map { resultRowToSimpleView(it) }
        }.singleOrNull()
    }

    override suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewModified {
        val id = UUID.randomUUID().toString()
        dbQuery {
            Views.insert {
                it[Views.id] = id
                it[Views.name] = name
                it[Views.owner] = owner
            }
            CharactersView.batchInsert(characterIds) {
                this[CharactersView.viewId] = id
                this[CharactersView.characterId] = it
            }
        }
        return ViewModified(id, characterIds)
    }

    override suspend fun edit(id: String, name: String, characters: List<Long>): ViewModified {
        dbQuery {
            Views.update({ Views.id.eq(id) }) {
                it[Views.name] = name
            }
            CharactersView.deleteWhere { viewId.eq(id) }
            CharactersView.batchInsert(characters) {
                this[CharactersView.viewId] = id
                this[CharactersView.characterId] = it
            }
        }
        return ViewModified(id, characters)
    }

    override suspend fun delete(id: String): ViewDeleted {
        dbQuery { Views.deleteWhere { Views.id.eq(id) } }
        return ViewDeleted(id)
    }

    override suspend fun getViews(): List<SimpleView> {
        return dbQuery { Views.selectAll().map { resultRowToSimpleView(it) } }
    }

    override suspend fun state(): List<SimpleView> {
        return dbQuery { Views.selectAll().map { resultRowToSimpleView(it) } }
    }
}