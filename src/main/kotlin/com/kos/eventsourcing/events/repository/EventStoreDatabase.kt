package com.kos.eventsourcing.events.repository

import com.kos.eventsourcing.events.Event
import com.kos.eventsourcing.events.EventData
import com.kos.eventsourcing.events.EventWithVersion
import com.kos.eventsourcing.events.ViewToBeCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.sequences.Sequence

class EventStoreDatabase(private val db: Database) : EventStore {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(EventData::class) {
                subclass(ViewToBeCreated::class, ViewToBeCreated.serializer())
            }
        }
        ignoreUnknownKeys = true
    }

    object Events : Table() {
        val version = long("version")
        val aggregateRoot = varchar("aggregate_root", 128)
        val eventType = varchar("event_type", 48)
        val data = text("data")

        override val primaryKey = PrimaryKey(version)
        override val tableName = "events"
    }

    private fun resultRowToEventWithVersion(row: ResultRow): EventWithVersion =
        EventWithVersion(
            row[Events.version],
            Event(row[Events.aggregateRoot], json.decodeFromString(row[Events.data]))
        )


    override suspend fun save(event: Event): EventWithVersion {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            val id = selectNextId()
            Events.insert {
                it[aggregateRoot] = event.aggregateRoot
                it[version] = id
                it[eventType] = event.eventData.eventType.toString()
                it[data] = json.encodeToString(event.eventData)
            }.resultedValues?.map { resultRowToEventWithVersion(it) }?.singleOrNull() ?: throw Exception(":(")
        }
    }

    override suspend fun getEvents(version: Long?): Sequence<EventWithVersion> {
        TODO("Not yet implemented")
    }

    override suspend fun state(): List<EventWithVersion> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Events.selectAll().map { resultRowToEventWithVersion(it) }
        }
    }

    override suspend fun withState(initialState: List<EventWithVersion>): EventStore {
        TODO("Not yet implemented")
    }

    private suspend fun selectNextId(): Long =
        newSuspendedTransaction(Dispatchers.IO, db) {
            TransactionManager.current().exec("""select nextval('event_versions') as id""") { rs ->
                if (rs.next()) rs.getLong("id")
                else -1
            }
        } ?: -1
}