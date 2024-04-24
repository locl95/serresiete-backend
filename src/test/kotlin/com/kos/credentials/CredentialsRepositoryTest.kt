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
            repository.insertCredentials(encryptedCredentials)
            assertTrue(repository.state().users.size == 1)
            assertTrue(repository.state().users.all { it.userName == user && it.password == encryptedCredentials.password })
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

    /*@Test
    open fun `given a repository with credentials, roles and activities i can retrieve the activities that an user is allowed to perform`() {
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
    } */

    @Test
    open fun `given a repository and a user with roles i can retrieve it's roles`() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    credentialsRoles = mapOf(user to listOf("role1", "role2"))
                )
            )

            val roles = repositoryWithState.getUserRoles(user)
            assertEquals(listOf("role1", "role2"), roles)
        }
    }

    @Test
    open fun `given a repository and roles i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(
                basicCredentialsInitialState.copy(
                    credentialsRoles = mapOf(user to listOf("role1", "role2"), "user2" to listOf("role1", "role3"))
                )
            )

            val roles = repositoryWithState.getRoles()
            assertEquals(setOf("role1", "role2", "role3"), roles)
        }
    }

    @Test
    open fun `given a repository with users i can add a role`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState)
            val initialRoles = repositoryWithState.state().credentialsRoles[user]
            repositoryWithState.insertRole(user, "role2")
            val finalState = repositoryWithState.state().credentialsRoles[user]
            assertEquals(emptyList(), initialRoles.orEmpty())
            assertEquals(listOf("role2"), finalState.orEmpty())
        }
    }

    @Test
    open fun `given a repository with users and roles i can delete a role`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicCredentialsInitialState.copy(
                credentialsRoles = mapOf(user to listOf("role1", "role2"), "user2" to listOf("role1", "role3"))
            ))
            val initialRolesUser = repositoryWithState.state().credentialsRoles[user]
            val initialRolesUser2 = repositoryWithState.state().credentialsRoles["user2"]
            repositoryWithState.deleteRole(user, "role2")
            val finalRolesUser = repositoryWithState.state().credentialsRoles[user]
            val finalRolesUser2 = repositoryWithState.state().credentialsRoles["user2"]
            assertEquals(listOf("role1", "role2"), initialRolesUser.orEmpty())
            assertEquals(listOf("role1", "role3"), initialRolesUser2.orEmpty())
            assertEquals(listOf("role1"), finalRolesUser)
            assertEquals(initialRolesUser2, finalRolesUser2)
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
