package com.kos.auth

import arrow.core.Either
import arrow.core.contains
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthDatabaseRepositoryTest : AuthRepositoryTest {

    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }

    override fun ICanGetAuthorizations() {
        runBlocking {
            val repository = AuthDatabaseRepository().withState(listOf(basicAuthorization))
            assertEquals(repository.getAuthorization(basicAuthorization.token), basicAuthorization)
        }
    }

    @Test
    override fun ICanInsertAuthorizations() {
        runBlocking {
            val repository = AuthDatabaseRepository()
            val userName = repository.insertToken("test")?.userName
            assertEquals("test", userName)
            val finalStateOfAuthorizations = repository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, "test")
        }
    }

    @Test
    override fun ICanDeleteAuthorizations() {
        runBlocking {
            val repository = AuthDatabaseRepository().withState(listOf(basicAuthorization))
            assertTrue(repository.deleteToken("test"))
            assertTrue(repository.state().isEmpty())
        }
    }
}