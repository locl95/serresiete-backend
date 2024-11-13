package com.kos.roles

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class RolesControllerTest {
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesRepository = RolesInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
    private val role = Role.USER

    private suspend fun createController(
        rolesState: List<Role>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): RolesController {
        rolesRepository.withState(rolesState)
        rolesActivitiesRepository.withState(rolesActivitiesState)

        val rolesService = RolesService(rolesRepository, rolesActivitiesRepository)
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
                listOf(role),
                mapOf(Pair(role, setOf(Activities.getAnyRoles)))
            )
            assertEquals(
                listOf(role),
                controller.getRoles("owner", setOf(Activities.getAnyRoles)).getOrNull()
            )
        }
    }

    @Test
    fun `i can get role`() {
        runBlocking {
            val expected = Pair(role, setOf(Activities.getAnyRoles))
            val controller = createController(
                listOf(role),
                mapOf(expected)
            )
            assertEquals(
                expected,
                controller.getRole("owner", setOf(Activities.getAnyRoles), role).getOrNull()
            )
        }
    }

    @Test
    fun `i can set activities to role`() {
        runBlocking {
            val expected = Pair(role, setOf(Activities.getAnyRoles))
            val controller = createController(
                listOf(role),
                mapOf(expected)
            )
            controller.setActivities(
                "owner",
                setOf(Activities.addActivityToRole),
                role,
                ActivitiesRequest(setOf(Activities.getAnyRoles))
            ).onLeft {
                fail(it.toString())
            }
        }
    }
}