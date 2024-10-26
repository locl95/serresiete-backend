package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.auth.repository.AuthRepository
import com.kos.common.DatabaseFactory
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AuthRepositoryTestCommon {

    abstract val repository: AuthRepository

    @Test
    fun `given a repository with one authorization i can retrieve it`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicAuthorization))
            assertEquals(repositoryWithState.getAuthorization(token), basicAuthorization)
        }
    }

    @Test
    fun `given an empty repository i can insert an authorization`() {
        runBlocking {
            val userName = repository.insertToken(user, isAccess = true)?.userName
            assertEquals(user, userName)
            val finalStateOfAuthorizations = repository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, user)
        }
    }

    @Test
    fun `given a repository with one authorization i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicAuthorization))
            assertTrue(repositoryWithState.deleteTokensFromUser(basicAuthorization.userName))
            assertTrue(repositoryWithState.state().isEmpty())
        }
    }
}

class AuthInMemoryRepositoryTest : AuthRepositoryTestCommon() {
    override val repository = AuthInMemoryRepository()

    @BeforeEach
    fun beforeEach() {
        repository.clear()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthDatabaseRepositoryTest : AuthRepositoryTestCommon() {

    private val embeddedPostgres = EmbeddedPostgres.start()

    private val flyway = Flyway
        .configure()
        .locations("db/migration/test")
        .dataSource(embeddedPostgres.postgresDatabase)
        .cleanDisabled(false)
        .load()

    override val repository: AuthRepository =
        AuthDatabaseRepository(Database.connect(embeddedPostgres.postgresDatabase))

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
