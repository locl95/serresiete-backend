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
import com.kos.views.ViewsTestHelper.owner
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
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner)),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.getAnyCredentials)))
            )

            controller.getCredentials(owner)
                .onRight { assertEquals(listOf(basicCredentials.copy(userName = owner)), it) }
                .onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner)),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.createCredentials)))
            )
            controller.createCredential(owner, basicCredentials).onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i can edit credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner), basicCredentials),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.editCredentials)))
            )
            controller.editCredential(owner, basicCredentials.copy(password = "password"))
                .onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i can delete credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner), basicCredentials),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.deleteCredentials)))
            )
            controller.deleteCredential(owner, owner).onLeft { fail() }
        }
    }

    @Test
    fun `i can get user roles`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner), basicCredentials),
                mapOf(Pair(owner, listOf(Role.USER)), Pair("someone", listOf(Role.ADMIN)))
            )

            val controller = createController(
                credentialsState,
                mapOf(
                    Pair(Role.USER, setOf(Activities.getAnyCredentialsRoles)),
                    Pair(Role.ADMIN, setOf(Activities.getOwnCredentialsRoles))
                )
            )
            controller.getUserRoles(owner, owner)
                .onRight { assertEquals((listOf(Role.USER)), it) }
                .onLeft { fail(it.toString()) }
            controller.getUserRoles(owner, "someone")
                .onRight { assertEquals((listOf(Role.ADMIN)), it) }
                .onLeft { fail(it.toString()) }
            controller.getUserRoles("someone", "someone")
                .onRight { assertEquals((listOf(Role.ADMIN)), it) }
                .onLeft { fail(it.toString()) }
            controller.getUserRoles("someone", owner)
                .onRight { fail() }
                .onLeft {
                    assertTrue(it is NotEnoughPermissions)
                    assertEquals("someone", it.user)
                }
        }
    }

    @Test
    fun `i can add a role to a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner), basicCredentials),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.addRoleToUser)))
            )
            controller.addRoleToUser(owner, owner, Role.ADMIN).onLeft { fail() }
        }
    }

    @Test
    fun `i can delete a role from a user`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = owner), basicCredentials),
                mapOf(Pair(owner, listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.deleteRoleFromUser)))
            )
            controller.deleteRoleFromUser(owner, owner, Role.USER).onLeft { fail() }
        }
    }

}