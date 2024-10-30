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
        activitiesState: Set<Activity>,
        rolesActivitiesState: Map<Role, Set<Activity>>
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
                setOf("do this", "do that"),
                mapOf(Pair(role, setOf(Activities.getAnyActivities)))
            )
            assertEquals(setOf("do this", "do that"), controller.getActivities("owner", setOf(Activities.getAnyActivities)).getOrNull())
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
                setOf(),
                mapOf(Pair(role, setOf(Activities.createActivities)))
            )
            assertTrue(controller.createActivity("owner", ActivityRequest("do this"), setOf(Activities.createActivities)).isRight())
            assertEquals(setOf("do this"), activitiesRepository.state())
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
                setOf("do this"),
                mapOf(Pair(role, setOf(Activities.deleteActivities)))
            )
            assertTrue(controller.deleteActivity("owner", "do this", setOf(Activities.deleteActivities)).isRight())
            assertEquals(setOf(), activitiesRepository.state())
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
                setOf("do this"),
                mapOf(Pair(role, setOf(Activities.getAnyActivities)), Pair("other-role", setOf("do this")))
            )
            assertEquals(setOf("do this"), controller.getActivitiesFromRole("owner", "other-role", setOf(Activities.getAnyActivities)).getOrNull())
        }
    }

}