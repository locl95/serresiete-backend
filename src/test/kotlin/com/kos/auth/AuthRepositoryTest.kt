package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.auth.repository.AuthRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AuthRepositoryTestCommon {

    abstract val repository: AuthRepository
    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun ICanGetAuthorizations() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicAuthorization))
            assertEquals(repositoryWithState.getAuthorization(token), basicAuthorization)
        }
    }

    @Test
    fun ICanInsertAuthorizations() {
        runBlocking {
            val userName = repository.insertToken(user, isAccess = true)?.userName
            assertEquals(user, userName)
            val finalStateOfAuthorizations = repository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, user)
        }
    }

    @Test
    fun ICanDeleteAuthorizations() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicAuthorization))
            assertTrue(repositoryWithState.deleteToken(token))
            assertTrue(repositoryWithState.state().isEmpty())
        }
    }
}

class AuthInMemoryRepositoryTest : AuthRepositoryTestCommon() {
    override val repository = AuthInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class AuthDatabaseRepositoryTest : AuthRepositoryTestCommon() {
    override val repository: AuthRepository = AuthDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
