package com.kos.views.repository

import com.kos.common.DatabaseFactory.dbQuery
import com.kos.views.Game
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
                this[Views.published] = it.published
                this[Views.game] = it.game.toString()
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
        val published = bool("published")
        val game = varchar("game", 3)

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToSimpleView(row: ResultRow): SimpleView {
        return SimpleView(
            row[Views.id],
            row[Views.name],
            row[Views.owner],
            row[Views.published],
            CharactersView.select { CharactersView.viewId.eq(row[Views.id]) }
                .map { resultRowToCharacterView(it).first },
            Game.fromString(row[Views.game])
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

    override suspend fun create(name: String, owner: String, characterIds: List<Long>, game: Game): ViewModified {
        val id = UUID.randomUUID().toString()
        dbQuery {
            Views.insert {
                it[Views.id] = id
                it[Views.name] = name
                it[Views.owner] = owner
                it[Views.published] = true
                it[Views.game] = game.toString()
            }
            CharactersView.batchInsert(characterIds) {
                this[CharactersView.viewId] = id
                this[CharactersView.characterId] = it
            }
        }
        return ViewModified(id, characterIds)
    }

    override suspend fun edit(id: String, name: String, published: Boolean, characters: List<Long>): ViewModified {
        dbQuery {
            Views.update({ Views.id.eq(id) }) {
                it[Views.name] = name
                it[Views.published] = published
            }
            CharactersView.deleteWhere { viewId.eq(id) }
            CharactersView.batchInsert(characters) {
                this[CharactersView.viewId] = id
                this[CharactersView.characterId] = it
            }
        }
        return ViewModified(id, characters)
    }

    override suspend fun patch(id: String, name: String?, published: Boolean?, characters: List<Long>?): ViewModified {
        dbQuery {
            Views.update({ Views.id.eq(id) }) { statement ->
                name?.let { statement[Views.name] = it }
                published?.let { statement[Views.published] = it }
            }
            characters?.let {
                CharactersView.deleteWhere { viewId.eq(id) }
                CharactersView.batchInsert(it) { cid ->
                    this[CharactersView.viewId] = id
                    this[CharactersView.characterId] = cid
                }
            }
        }
        return ViewModified(id, characters.orEmpty()) //TODO: Fix this
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