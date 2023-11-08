package com.kos.credentials

import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CredentialsInMemoryRepositoryTest : CredentialsRepositoryTest {
    @Test
    override fun ICanGetCredentials() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            assertEquals(credentialsInMemoryRepository.getCredentials(user), Credentials(user, password))
        }
    }

    @Test
    override fun ICanInsertCredentials() {
        runBlocking {
            val repository = CredentialsInMemoryRepository()
            assertTrue(repository.state().users.isEmpty())
            repository.insertCredentials(basicCredentials)
            assertTrue(repository.state().users.size == 1)
            assertTrue(repository.state().users.all { it.userName == user && it.password == password } )
        }
    }

    @Test
    override fun ICanEditCredentials() {
        runBlocking {
            val repository = CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            assertTrue(repository.state().users.size == 1)
            repository.editCredentials(user, "newPassword")
            assertTrue(repository.state().users.all { it.userName == user && it.password == "newPassword" } )
        }
    }

    @Test
    override fun ICanGetActivities() {
        runBlocking {
            val repository = CredentialsInMemoryRepository().withState(
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