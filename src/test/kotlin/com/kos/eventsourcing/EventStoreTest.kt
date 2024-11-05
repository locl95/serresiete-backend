package com.kos.eventsourcing

import com.kos.eventsourcing.events.Event
import com.kos.eventsourcing.events.EventWithVersion
import com.kos.eventsourcing.events.ViewToBeCreated
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.eventsourcing.events.repository.EventStoreDatabase
import com.kos.eventsourcing.events.repository.EventStoreInMemory
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


abstract class EventStoreTest {

    abstract val store: EventStore

    @Test
    fun `given an empty store i can save events`() {
        runBlocking {
            val payload = ViewToBeCreated(
                UUID.randomUUID().toString(),
                basicSimpleWowView.name,
                basicSimpleWowView.published,
                listOf(),
                basicSimpleWowView.game,
                basicSimpleWowView.owner
            )
            val event1 = Event("/credentials/client1", UUID.randomUUID().toString(), payload)
            val event2 = Event("/credentials/client1", UUID.randomUUID().toString(), payload)
            store.save(event1)
            store.save(event2)
            val expected = listOf(EventWithVersion(1, event1), EventWithVersion(2, event2))

            assertEquals(expected, store.state())
        }
    }
}

class EventStoreInMemoryTest : EventStoreTest() {
    override val store = EventStoreInMemory()

    @BeforeEach
    fun beforeEach() {
        store.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventStoreDatabaseTest : EventStoreTest() {
    private val embeddedPostgres = EmbeddedPostgres.start()

    private val flyway = Flyway
        .configure()
        .locations("db/migration/test")
        .dataSource(embeddedPostgres.postgresDatabase)
        .cleanDisabled(false)
        .load()

    override val store = EventStoreDatabase(Database.connect(embeddedPostgres.postgresDatabase))

    @BeforeEach
    fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @AfterAll
    fun afterAll() {
        embeddedPostgres.close()
    }
}