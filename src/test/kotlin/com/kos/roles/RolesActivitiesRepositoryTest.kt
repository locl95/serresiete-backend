package com.kos.roles

import com.kos.activities.ActivitiesTestHelper.basicActivity
import com.kos.roles.RolesTestHelper.basicRolesActivities
import com.kos.roles.repository.RolesActivitiesDatabaseRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesActivitiesRepository
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class RolesActivitiesRepositoryTest {

    abstract val repository: RolesActivitiesRepository

    @Test
    fun `given a repository with roles and activities i can retrieve activities from a given role`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicRolesActivities)
            assertEquals(repositoryWithState.getActivitiesFromRole(Role.USER), setOf(basicActivity))
        }
    }

    @Test
    fun `given a repository i can insert an activity to it`() {
        runBlocking {
            repository.insertActivityToRole(basicActivity, Role.USER)
            assertEquals(repository.state(), basicRolesActivities)
        }
    }

    @Test
    fun `given a repository i can insert activities to it`() {
        val anotherActivity = "another activity"
        runBlocking {
            repository.insertActivityToRole(basicActivity, Role.USER)
            repository.insertActivityToRole(anotherActivity, Role.USER)
            val expected = mapOf(Pair(Role.USER, setOf(basicActivity, anotherActivity)))
            assertEquals(expected, repository.state())
        }
    }

    @Test
    fun `given a repository with one role and 1 activity i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicRolesActivities)
            repositoryWithState.deleteActivityFromRole(basicActivity, Role.USER)
            assertEquals(setOf(), repositoryWithState.state()[Role.USER].orEmpty())
        }
    }
}

class RolesActivitiesInMemoryRepositoryTest : RolesActivitiesRepositoryTest() {
    override val repository = RolesActivitiesInMemoryRepository()

    @BeforeEach
    fun beforeEach() {
        repository.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RolesActivitiesDatabaseRepositoryTest : RolesActivitiesRepositoryTest() {
    private val embeddedPostgres = EmbeddedPostgres.start()

    private val flyway = Flyway
        .configure()
        .locations("db/migration/test")
        .dataSource(embeddedPostgres.postgresDatabase)
        .cleanDisabled(false)
        .load()

    override val repository = RolesActivitiesDatabaseRepository(Database.connect(embeddedPostgres.postgresDatabase))

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
