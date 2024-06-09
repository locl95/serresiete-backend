package com.kos.credentials

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.common.NotEnoughPermissions
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.Role
import com.kos.roles.RolesTestHelper.role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CredentialsControllerTest {
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        rolesActivitiesState: Map<Role, List<Activity>>
    ): CredentialsController {
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        return CredentialsController(credentialsService)
    }


    @BeforeTest
    fun beforeEach() {
        credentialsRepository.clear()
        rolesActivitiesRepository.clear()
    }

    @Test
    fun `i can get credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.getAnyCredentials)))
            )
            assertEquals(
                listOf(basicCredentials.copy(userName = "owner")),
                controller.getCredentials("owner").getOrNull()
            )
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.createCredentials)))
            )
            assertTrue(controller.createCredential("owner", basicCredentials).isRight())
        }
    }

    @Test
    fun `i can edit credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.editCredentials)))
            )
            assertTrue(controller.editCredential("owner", basicCredentials.copy(password = "password")).isRight())
        }
    }

    @Test
    fun `i can delete credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.deleteCredentials)))
            )
            assertTrue(controller.deleteCredential("owner", "owner").isRight())
        }
    }

    @Test
    fun `i can get user roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(role)), Pair("someone", listOf("something")))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.getAnyCredentialsRoles)),Pair("something", listOf(Activities.getOwnCredentialsRoles)))
            )
            assertEquals(listOf(role), controller.getUserRoles("owner", "owner").getOrNull())
            assertEquals(listOf("something"), controller.getUserRoles("owner", "someone").getOrNull())
            assertEquals(listOf("something"), controller.getUserRoles("someone", "someone").getOrNull())
            assertEquals(NotEnoughPermissions("someone"), controller.getUserRoles("someone", "owner").getLeftOrNull())
        }
    }

    @Test
    fun `i can add a role to a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.addRoleToUser)))
            )
            assertTrue(controller.addRoleToUser("owner", "owner", "something").isRight())
        }
    }

    @Test
    fun `i can delete a role from a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, listOf(Activities.deleteRoleFromUser)))
            )
            assertTrue(controller.deleteRoleFromUser("owner", "owner", role).isRight())
        }
    }

}