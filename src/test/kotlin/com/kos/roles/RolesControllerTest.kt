package com.kos.roles

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.activities.ActivityRequest
import com.kos.credentials.repository.CredentialsInMemoryRepository
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
    private val role = Role.USER

    private suspend fun createController(
        rolesState: List<Role>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): RolesController {
        val rolesRepositoryWithState = rolesRepository.withState(rolesState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val rolesService = RolesService(rolesRepositoryWithState, rolesActivitiesRepositoryWithState)
        return RolesController(rolesService)
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
            val controller = createController(
                listOf(Role.USER),
                mapOf(Pair(role, setOf(Activities.getAnyRoles)))
            )
            assertEquals(
                listOf(role),
                controller.getRoles("owner", setOf(Activities.getAnyRoles)).getOrNull()
            )
        }
    }
}