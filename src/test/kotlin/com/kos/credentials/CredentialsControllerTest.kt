package com.kos.credentials

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.common.CantDeleteYourself
import com.kos.common.NotEnoughPermissions
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRoles
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
import com.kos.credentials.CredentialsTestHelper.emptyCredentialsState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.Role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*


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

            val controller = createController(
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            assertEquals(
                listOf(basicCredentialsWithRoles),
                controller.getCredentials("owner", setOf(Activities.getAnyCredentials)).getOrNull()
            )
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val controller = createController(
                emptyCredentialsState,
                mapOf()
            )

            assertTrue(
                controller.createCredential(
                    "owner",
                    setOf(Activities.createCredentials),
                    CreateCredentialRequest(user, password, setOf())
                ).isRight()
            )
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

            assertTrue(
                controller.editCredential(
                    "owner",
                    setOf(Activities.editCredentials),
                    "owner",
                    EditCredentialRequest("password", setOf())
                ).isRight()
            )
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

            assertTrue(controller.deleteCredential("owner", setOf(Activities.deleteCredentials), user).isRight())
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

            assertEquals(
                listOf(Role.ADMIN),
                controller.getUserRoles("owner", setOf(Activities.getAnyCredentialsRoles), "owner").getOrNull()
            )
            assertEquals(
                listOf(Role.USER),
                controller.getUserRoles("owner", setOf(Activities.getAnyCredentialsRoles), "someone").getOrNull()
            )
            assertEquals(
                listOf(Role.USER),
                controller.getUserRoles("someone", setOf(Activities.getOwnCredentialsRoles), "someone").getOrNull()
            )
            assertEquals(
                NotEnoughPermissions("someone"),
                controller.getUserRoles("someone", setOf(Activities.getOwnCredentialsRoles), "owner").getLeftOrNull()
            )
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

            assertTrue(
                controller.addRoleToUser("owner", setOf(Activities.addRoleToUser), "owner", Role.ADMIN).isRight()
            )
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

            assertTrue(
                controller.deleteRoleFromUser("owner", setOf(Activities.deleteRoleFromUser), "owner", Role.USER)
                    .isRight()
            )
        }
    }

    @Test
    fun `i cant remove my own credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf()
            )

            controller.deleteCredential("owner", setOf(Activities.deleteCredentials), "owner")
                .onRight { fail("expected failure") }
                .onLeft { assertTrue(it is CantDeleteYourself) }
        }
    }

}