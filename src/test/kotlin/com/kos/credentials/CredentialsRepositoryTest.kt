package com.kos.credentials

import com.kos.common.isDefined
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
import com.kos.credentials.CredentialsTestHelper.encryptedCredentials
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsDatabaseRepository
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepository
import com.kos.roles.Role
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class CredentialsRepositoryTest {

    abstract val repository: CredentialsRepository

    @Test
    open fun `given a repository with credentials i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            assertEquals(repositoryWithState.getCredentials(user), encryptedCredentials)
        }
    }

    @Test
    open fun `given an empty repository i can insert credentials`() {
        runBlocking {
            assertTrue(repository.state().users.isEmpty())
            repository.insertCredentials(encryptedCredentials.userName, encryptedCredentials.password)
            assertTrue(repository.state().users.size == 1)
            assertTrue(repository.state().users.all { it.userName == user && it.password == encryptedCredentials.password })
        }
    }

    @Test
    open fun `given an empty repository i can batch insert roles to a credential`() {
        runBlocking {
            assertTrue(repository.state().users.isEmpty())
            repository.insertRoles(user, setOf(Role.USER, Role.ADMIN, Role.SERVICE))
            assertEquals(3, repository.state().credentialsRoles[user]?.size)
            assertEquals(listOf(Role.USER, Role.ADMIN, Role.SERVICE), repository.state().credentialsRoles[user])
        }
    }

    @Test
    open fun `given a repository with credentials i can edit them`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            repositoryWithState.editCredentials(user, "newPassword")
            assertTrue(repositoryWithState.state().users.contains(Credentials(user, "newPassword")))
        }
    }

    @Test
    open fun `given a repository and a user with roles i can retrieve it's roles`() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    credentialsRoles = mapOf(user to listOf(Role.USER, Role.ADMIN))
                )
            )

            val roles = repositoryWithState.getUserRoles(user)
            assertEquals(listOf(Role.USER, Role.ADMIN), roles)
        }
    }

    @Test
    open fun `given a repository with credentials i can delete them`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            repositoryWithState.deleteCredentials(user)

            assertEquals(listOf(), repositoryWithState.state().users)
        }
    }

    @Test
    open fun `given a repository with users i can add a role`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            val initialRoles = repositoryWithState.state().credentialsRoles[user]
            repositoryWithState.insertRole(user, Role.ADMIN)
            val finalState = repositoryWithState.state().credentialsRoles[user]
            assertEquals(emptyList(), initialRoles.orEmpty())
            assertEquals(listOf(Role.ADMIN), finalState.orEmpty())
        }
    }

    @Test
    open fun `given a repository with users and roles i can delete a role`() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    credentialsRoles = mapOf(user to listOf(Role.USER, Role.ADMIN), "user2" to listOf(Role.USER, Role.SERVICE)))
                )
            val initialRolesUser = repositoryWithState.state().credentialsRoles[user]
            val initialRolesUser2 = repositoryWithState.state().credentialsRoles["user2"]
            repositoryWithState.deleteRole(user, Role.ADMIN)
            val finalRolesUser = repositoryWithState.state().credentialsRoles[user]
            val finalRolesUser2 = repositoryWithState.state().credentialsRoles["user2"]
            assertEquals(listOf(Role.USER, Role.ADMIN), initialRolesUser.orEmpty())
            assertEquals(listOf(Role.USER, Role.SERVICE), initialRolesUser2.orEmpty())
            assertEquals(listOf(Role.USER), finalRolesUser)
            assertEquals(initialRolesUser2, finalRolesUser2)
        }
    }

    @Test
    open fun `given a repository with users and roles i can delete all role`() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    credentialsRoles = mapOf(user to listOf(Role.USER, Role.ADMIN), "user2" to listOf(Role.USER, Role.SERVICE)))
            )

            repositoryWithState.deleteRoles("user2")
            assertEquals(null, repositoryWithState.state().credentialsRoles["user2"])
            assertEquals(listOf(Role.USER, Role.ADMIN), repositoryWithState.state().credentialsRoles[user])
        }
    }

    @Test
    open fun `given a repository with credentials and roles i can patch them`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsWithRolesInitialState)
            repositoryWithState.patch(user, PatchCredentialRequest("newPassword", setOf()))
            assertTrue(repositoryWithState.state().users.contains(Credentials(user, "newPassword")))
            assertFalse(repositoryWithState.state().credentialsRoles[user].isDefined())
        }
    }
}

class CredentialsInMemoryRepositoryTest : CredentialsRepositoryTest() {
    override val repository = CredentialsInMemoryRepository()

    @BeforeEach
    fun beforeEach() {
        repository.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest() {
    private val embeddedPostgres = EmbeddedPostgres.start()

    private val flyway = Flyway
        .configure()
        .locations("db/migration/test")
        .dataSource(embeddedPostgres.postgresDatabase)
        .cleanDisabled(false)
        .load()

    override val repository: CredentialsRepository =
        CredentialsDatabaseRepository(Database.connect(embeddedPostgres.postgresDatabase))

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
