package com.kos.activities

import com.kos.activities.repository.ActivitiesInMemoryRepository
import com.kos.credentials.CredentialsService
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


class ActivitiesControllerTest {
    private val activitiesRepository = ActivitiesInMemoryRepository()

    private suspend fun createController(
        activitiesState: Set<Activity>,
    ): ActivitiesController {
        val activitiesRepositoryWithState = activitiesRepository.withState(activitiesState)

        val activitiesService = ActivitiesService(activitiesRepositoryWithState)
        return ActivitiesController(activitiesService)
    }


    @BeforeTest
    fun beforeEach() {
        activitiesRepository.clear()
    }

    @Test
    fun `i can get activities`() {
        runBlocking {
            val controller = createController(setOf("do this", "do that"))
            assertEquals(
                setOf("do this", "do that"),
                controller.getActivities("owner", setOf(Activities.getAnyActivities)).getOrNull()
            )
        }
    }

}