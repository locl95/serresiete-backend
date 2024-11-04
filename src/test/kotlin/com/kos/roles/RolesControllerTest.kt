package com.kos.roles

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
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
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                listOf(Role.USER),
                mapOf(Pair(Role.USER, setOf(Activities.getAnyRoles)))
            )
            assertEquals(
                listOf(Role.USER),
                controller.getRoles("owner").getOrNull()
            )
        }
    }

    @Test
    fun `i can create roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                listOf(Role.USER),
                mapOf(Pair(Role.USER, setOf(Activities.createRoles)))
            )
            assertTrue(controller.createRole("owner", RoleRequest(Role.ADMIN)).isRight())
        }
    }

    @Test
    fun `i can delete roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                listOf(Role.USER),
                mapOf(Pair(Role.USER, setOf(Activities.deleteRoles)))
            )
            assertTrue(controller.deleteRole("owner", Role.USER).isRight())
        }
    }

    @Test
    fun `i can add activity to role`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                listOf(Role.USER),
                mapOf(Pair(Role.USER, setOf(Activities.addActivityToRole)))
            )
            assertTrue(controller.addActivityToRole("owner", ActivityRequest("something"), Role.USER).isRight())
        }
    }

    @Test
    fun `i can remove activity from role`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                listOf(Role.USER),
                mapOf(Pair(Role.USER, setOf(Activities.deleteActivityFromRole)))
            )
            assertTrue(
                controller.deleteActivityFromRole("owner", Role.USER, Activities.deleteActivityFromRole).isRight()
            )
        }
    }

}