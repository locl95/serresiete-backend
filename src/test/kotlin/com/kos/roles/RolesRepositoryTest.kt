package com.kos.roles

import RolesRepository
import com.kos.roles.repository.RolesDatabaseRepository
import com.kos.roles.repository.RolesInMemoryRepository
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.test.*

abstract class RolesRepositoryTestCommon {

    abstract val repository: RolesRepository

    @Test
    fun `given a repository with roles i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(Role.USER))
            assertEquals(repositoryWithState.getRoles(), listOf(Role.USER))
        }
    }

    @Test
    fun `given an empty repository i can insert a role`() {
        runBlocking {
            repository.insertRole(Role.USER)
            val state = repository.state()
            assertContains(state, Role.USER)
        }
    }

    @Test
    fun `given a repository with one role i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(Role.USER))
            repositoryWithState.deleteRole(Role.USER)
            assertTrue(repositoryWithState.state().isEmpty())
        }
    }
}

class RolesInMemoryRepositoryTest : RolesRepositoryTestCommon() {
    override val repository = RolesInMemoryRepository()

    @BeforeEach
    fun beforeEach() {
        repository.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RolesDatabaseRepositoryTest : RolesRepositoryTestCommon() {
    private val embeddedPostgres = EmbeddedPostgres.start()

    private val flyway = Flyway
        .configure()
        .locations("db/migration/test")
        .dataSource(embeddedPostgres.postgresDatabase)
        .cleanDisabled(false)
        .load()

    override val repository = RolesDatabaseRepository(Database.connect(embeddedPostgres.postgresDatabase))

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
