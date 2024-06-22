package com.kos.activities

import com.kos.activities.repository.ActivitiesInMemoryRepository
import com.kos.credentials.CredentialsService
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


class ActivitiesControllerTest {
    private val activitiesRepository = ActivitiesInMemoryRepository()
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()


    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        activitiesState: List<Activity>,
        rolesActivitiesState: Map<Role, List<Activity>>
    ): ActivitiesController {
        val activitiesRepositoryWithState = activitiesRepository.withState(activitiesState)
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val activitiesService = ActivitiesService(activitiesRepositoryWithState)
        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        return ActivitiesController(activitiesService, credentialsService)
    }


    @BeforeTest
    fun beforeEach() {
        activitiesRepository.clear()
        credentialsRepository.clear()
        rolesActivitiesRepository.clear()
    }

    @Test
    fun `i can get activities`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf("do this", "do that"),
                mapOf(Pair(role, listOf(Activities.getAnyActivities)))
            )
            assertEquals(listOf("do this", "do that"), controller.getActivities("owner").getOrNull())
        }
    }

    @Test
    fun `i can create activities`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(),
                mapOf(Pair(role, listOf(Activities.createActivities)))
            )
            assertTrue(controller.createActivity("owner", ActivityRequest("do this")).isRight())
            assertEquals(listOf("do this"), activitiesRepository.state())
        }
    }

    @Test
    fun `i can delete activities`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf("do this"),
                mapOf(Pair(role, listOf(Activities.deleteActivities)))
            )
            assertTrue(controller.deleteActivity("owner", "do this").isRight())
            assertEquals(listOf(), activitiesRepository.state())
        }
    }

    @Test
    fun `i can get activities from a role`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf("do this"),
                mapOf(Pair(role, listOf(Activities.getAnyActivities)), Pair("other-role", listOf("do this")))
            )
            assertEquals(setOf("do this"), controller.getActivitiesFromRole("owner", "other-role").getOrNull())
        }
    }

}