package com.kos.roles

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.RolesTestHelper.role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class RolesControllerTest {
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesRepository = RolesInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        rolesState: List<Role>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): RolesController {
        val rolesRepositoryWithState = rolesRepository.withState(rolesState)
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val rolesService = RolesService(rolesRepositoryWithState, rolesActivitiesRepositoryWithState)
        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        return RolesController(rolesService, credentialsService)
    }


    @BeforeTest
    fun beforeEach() {
        credentialsRepository.clear()
        rolesActivitiesRepository.clear()
        rolesRepository.clear()
    }

    @Test
    fun `i can get roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(role),
                mapOf(Pair(role, setOf(Activities.getAnyRoles)))
            )
            assertEquals(
                listOf(role),
                controller.getRoles("owner").getOrNull()
            )
        }
    }

    @Test
    fun `i can create roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(role),
                mapOf(Pair(role, setOf(Activities.createRoles)))
            )
            assertTrue(controller.createRole("owner", RoleRequest("something")).isRight())
        }
    }

    @Test
    fun `i can delete roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(role),
                mapOf(Pair(role, setOf(Activities.deleteRoles)))
            )
            assertTrue(controller.deleteRole("owner", role).isRight())
        }
    }

    @Test
    fun `i can add activity to role`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(role),
                mapOf(Pair(role, setOf(Activities.addActivityToRole)))
            )
            assertTrue(controller.addActivityToRole("owner", ActivityRequest("something"), role).isRight())
        }
    }

    @Test
    fun `i can remove activity from role`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(role),
                mapOf(Pair(role, setOf(Activities.deleteActivityFromRole)))
            )
            assertTrue(controller.deleteActivityFromRole("owner", Activities.deleteActivityFromRole, role).isRight())
        }
    }

}