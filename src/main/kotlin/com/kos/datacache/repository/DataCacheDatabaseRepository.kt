package com.kos.datacache.repository

import com.kos.common.DatabaseFactory.dbQuery
import com.kos.datacache.DataCache
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.OffsetDateTime

class DataCacheDatabaseRepository : DataCacheRepository {

    override suspend fun withState(initialState: List<DataCache>): DataCacheDatabaseRepository {
        dbQuery {
            DataCaches.batchInsert(initialState) {
                this[DataCaches.characterId] = it.characterId
                this[DataCaches.data] = it.data
                this[DataCaches.inserted] = it.inserted.toString()
            }
        }
        return this
    }

    object DataCaches : Table() {
        val characterId = long("character_id")
        val data = text("data")
        val inserted = text("inserted")

        override val primaryKey = PrimaryKey(characterId)
        override val tableName = "data_cache"
    }

    private fun resultRowToDataCache(row: ResultRow) = DataCache(
        row[DataCaches.characterId],
        row[DataCaches.data],
        OffsetDateTime.parse(row[DataCaches.inserted]),
    )

    override suspend fun insert(data: List<DataCache>): Boolean {
        dbQuery {
            DataCaches.batchInsert(data) {
                this[DataCaches.characterId] = it.characterId
                this[DataCaches.data] = it.data
                this[DataCaches.inserted] = it.inserted.toString()
            }
        }
        return true
    }

    override suspend fun update(dataCache: DataCache): Boolean {
        dbQuery {
            DataCaches.update({ DataCaches.characterId.eq(dataCache.characterId) }) {
                it[characterId] = dataCache.characterId
                it[data] = dataCache.data
                it[inserted] = dataCache.inserted.toString()
            }
        }
        return true
    }

    override suspend fun get(characterId: Long): List<DataCache> =
        dbQuery {
            DataCaches.select { DataCaches.characterId.eq(characterId) }.map { resultRowToDataCache(it) }
        }

    override suspend fun deleteExpiredRecord(ttl: Long): Int {
        return dbQuery {
            DataCaches.deleteWhere { inserted.less(OffsetDateTime.now().minusHours(ttl).toString()) }
        }
    }

    override suspend fun state(): List<DataCache> = dbQuery { DataCaches.selectAll().map { resultRowToDataCache(it) } }
}