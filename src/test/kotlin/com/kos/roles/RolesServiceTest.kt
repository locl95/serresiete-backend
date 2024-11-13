package com.kos.roles

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class RolesServiceTest {
    private val rolesRepository = RolesInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()

    @BeforeEach
    fun beforeEach() {
        rolesRepository.clear()
        rolesActivitiesRepository.clear()
    }

    @Test
    fun `i can get roles`() {
        runBlocking {
            val expected = listOf(Role.USER)
            val service = createService(expected, mapOf())
            assertEquals(expected, service.getRoles())
        }
    }

    @Test
    fun `i can get role`() {
        runBlocking {
            val expected = Role.USER to setOf(Activities.getAnyRoles)
            val service = createService(listOf(Role.USER), mapOf(expected))
            assertEquals(expected, service.getRole(Role.USER))
        }
    }

    @Test
    fun `i can set activities to a role`() {
        runBlocking {
            val service = createService(listOf(), mapOf())
            val expected = setOf(Activities.addActivityToRole)
            service.setActivitiesToRole(Role.USER, expected)
            assertEquals(expected, rolesActivitiesRepository.state()[Role.USER])
        }
    }

    private suspend fun createService(
        rolesState: List<Role>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): RolesService {
        rolesRepository.withState(rolesState)
        rolesActivitiesRepository.withState(rolesActivitiesState)
        return RolesService(rolesRepository, rolesActivitiesRepository)
    }
}