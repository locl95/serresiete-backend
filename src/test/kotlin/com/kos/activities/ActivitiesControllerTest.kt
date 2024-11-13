package com.kos.activities

import com.kos.activities.repository.ActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


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