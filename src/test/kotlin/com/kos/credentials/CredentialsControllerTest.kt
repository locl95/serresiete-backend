package com.kos.credentials

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.common.NotEnoughPermissions
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.Role
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
        rolesActivitiesState: Map<Role, Set<Activity>>
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
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertEquals(
                listOf(basicCredentials.copy(userName = "owner")),
                controller.getCredentials("owner", setOf(Activities.getAnyCredentials)).getOrNull()
            )
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertTrue(controller.createCredential("owner", setOf(Activities.createCredentials), basicCredentials).isRight())
        }
    }

    @Test
    fun `i can edit credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertTrue(controller.editCredential("owner", setOf(Activities.editCredentials), basicCredentials.copy(password = "password")).isRight())
        }
    }

    @Test
    fun `i can delete credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertTrue(controller.deleteCredential("owner", setOf(Activities.deleteCredentials), "owner").isRight())
        }
    }

    @Test
    fun `i can get user roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(Role.ADMIN)), Pair("someone", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertEquals(listOf(Role.ADMIN), controller.getUserRoles("owner", setOf(Activities.getAnyCredentialsRoles),"owner").getOrNull())
            assertEquals(listOf(Role.USER), controller.getUserRoles("owner", setOf(Activities.getAnyCredentialsRoles), "someone").getOrNull())
            assertEquals(listOf(Role.USER), controller.getUserRoles("someone", setOf(Activities.getOwnCredentialsRoles),"someone").getOrNull())
            assertEquals(NotEnoughPermissions("someone"), controller.getUserRoles("someone", setOf(Activities.getOwnCredentialsRoles),"owner").getLeftOrNull())
        }
    }

    @Test
    fun `i can add a role to a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertTrue(controller.addRoleToUser("owner", setOf(Activities.addRoleToUser), "owner", Role.ADMIN).isRight())
        }
    }

    @Test
    fun `i can delete a role from a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            assertTrue(controller.deleteRoleFromUser("owner", setOf(Activities.deleteRoleFromUser), "owner", Role.USER).isRight())
        }
    }

}