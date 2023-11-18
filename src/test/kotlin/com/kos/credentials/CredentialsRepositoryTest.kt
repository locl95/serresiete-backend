package com.kos.credentials

import com.kos.common.DatabaseFactory
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.encryptedCredentials
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsDatabaseRepository
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class CredentialsRepositoryTest {

    abstract val repository: CredentialsRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    open fun ICanGetCredentials() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            assertEquals(repositoryWithState.getCredentials(user), encryptedCredentials)
        }
    }

    @Test
    open fun ICanInsertCredentials() {
        runBlocking {
            assertTrue(repository.state().users.isEmpty())
            repository.insertCredentials(encryptedCredentials)
            assertTrue(repository.state().users.size == 1)
            assertTrue(repository.state().users.all { it.userName == user && it.password == encryptedCredentials.password })
        }
    }

    @Test
    open fun ICanEditCredentials() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            repositoryWithState.editCredentials(user, "newPassword")
            assertTrue(repositoryWithState.state().users.contains(Credentials(user, "newPassword")))
        }
    }

    @Test
    open fun ICanGetActivities() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    users = basicCredentialsInitialState.users + Credentials("user2", "password"),
                    credentialsRoles = mapOf(user to listOf("role1"), "user2" to listOf("role2")),
                    rolesActivities = mapOf(
                        "role1" to listOf("login", "logout", "create a view"),
                        "role2" to listOf("get view data", "get view cached data")
                    )
                )
            )
            val userActivities = repositoryWithState.getActivities(user)
            val serviceActivities = repositoryWithState.getActivities("user2")
            assertEquals(setOf("login", "logout", "create a view"), userActivities.toSet())
            assertEquals(setOf("get view data", "get view cached data"), serviceActivities.toSet())
        }
    }
}

class CredentialsInMemoryRepositoryTest : CredentialsRepositoryTest() {
    override val repository = CredentialsInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest() {
    override val repository: CredentialsRepository = CredentialsDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
