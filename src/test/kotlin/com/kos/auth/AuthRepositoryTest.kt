package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.auth.repository.AuthRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AuthRepositoryTestCommon {

    abstract val repository: AuthRepository

    @Test
    fun ICanGetAuthorizations() {
        runBlocking {
            val repoWithState = repository.withState(listOf(basicAuthorization))
            assertEquals(repoWithState.getAuthorization(token), basicAuthorization)
        }
    }

    @Test
    fun ICanInsertAuthorizations() {
        runBlocking {
            val repoWithState = repository.withState(emptyList())
            val userName = repoWithState.insertToken(user, isAccess = true)?.userName
            assertEquals(user, userName)
            val finalStateOfAuthorizations = repoWithState.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, user)
        }
    }

    @Test
    fun ICanDeleteAuthorizations() {
        runBlocking {
            val repoWithState = repository.withState(listOf(basicAuthorization))
            assertTrue(repoWithState.deleteToken(token))
            assertTrue(repoWithState.state().isEmpty())
        }
    }
}

class AuthInMemoryRepositoryTest : AuthRepositoryTestCommon() {
    override val repository = AuthInMemoryRepository()
    @Before
    fun beforeEach() {
        repository.clear()
    }


}

class AuthDatabaseRepositoryTest : AuthRepositoryTestCommon() {
    override val repository: AuthRepository = AuthDatabaseRepository()
    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
