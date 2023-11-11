package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
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
            assertEquals(repository.getAuthorization(token), basicAuthorization)
        }
    }

    @Test
    override fun ICanInsertAuthorizations() {
        runBlocking {
            val repository = AuthDatabaseRepository()
            val userName = repository.insertToken(user, isAccess = true)?.userName
            assertEquals(user, userName)
            val finalStateOfAuthorizations = repository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, user)
        }
    }

    @Test
    override fun ICanDeleteAuthorizations() {
        runBlocking {
            val repository = AuthDatabaseRepository().withState(listOf(basicAuthorization))
            assertTrue(repository.deleteToken(token))
            assertTrue(repository.state().isEmpty())
        }
    }
}