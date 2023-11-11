package com.kos.credentials

import com.kos.common.DatabaseFactory
import com.kos.credentials.CredentialsTestHelper.encryptedCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsDatabaseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest {

    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }

    @Test
    override fun ICanGetCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(basicCredentialsInitialState)
            assertEquals(repository.getCredentials(user), encryptedCredentials)
        }
    }

    @Test
    override fun ICanInsertCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository()
            assertTrue(repository.state().users.isEmpty())
            repository.insertCredentials(encryptedCredentials)
            assertTrue(repository.state().users.size == 1)
            assertTrue(repository.state().users.all { it.userName == user && it.password == encryptedCredentials.password })
        }
    }

    @Test
    override fun ICanEditCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(basicCredentialsInitialState)
            repository.editCredentials(user, "newPassword")
            assertTrue(repository.state().users.contains(Credentials(user, "newPassword")))
        }
    }

    @Test
    override fun ICanGetActivities() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(
                basicCredentialsInitialState.copy(
                    users = basicCredentialsInitialState.users + Credentials("user2", "password"),
                    credentialsRoles = mapOf(user to listOf("role1"), "user2" to listOf("role2")),
                    rolesActivities = mapOf(
                        "role1" to listOf("login", "logout", "create a view"),
                        "role2" to listOf("get view data", "get view cached data")
                    )
                )
            )
            val userActivities = repository.getActivities(user)
            val serviceActivities = repository.getActivities("user2")
            assertEquals(setOf("login", "logout","create a view"), userActivities.toSet())
            assertEquals(setOf("get view data", "get view cached data"), serviceActivities.toSet())
        }
    }
}