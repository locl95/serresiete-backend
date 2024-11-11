package com.kos.credentials

import com.kos.activities.ActivitiesTestHelper.basicActivity
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRoles
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
import com.kos.credentials.CredentialsTestHelper.encryptedCredentials
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.roles.Role
import com.kos.roles.RolesTestHelper.basicRolesActivities
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CredentialsServiceTest {
    @Test
    fun `i can validate credentials`() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)
            assertTrue(credentialsService.validateCredentials(basicCredentials))
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository()
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            credentialsService.createCredentials(CreateCredentialsRequest(user, password, setOf()))
            assertEquals(credentialsInMemoryRepository.state().users.map { it.userName }, listOf(user))
        }
    }

    @Test
    fun `i can edit credentials`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            assertEquals(credentialsInMemoryRepository.getCredentials(user), encryptedCredentials)
            credentialsService.editCredentials(basicCredentials.copy(password = "newPassword"))
            assertNotEquals(credentialsInMemoryRepository.getCredentials(user)?.password, encryptedCredentials.password)
        }
    }

    @Test
    fun `i can get user roles`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsWithRolesInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            assertEquals(credentialsService.getUserRoles(user), listOf(Role.USER))
        }
    }

    @Test
    fun `i can add a role to a user`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            credentialsService.addRoleToUser(user, Role.USER)
            assertEquals(credentialsService.getUserRoles(user), listOf(Role.USER))
        }
    }

    @Test
    fun `i can delete a role from a user`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsWithRolesInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            credentialsService.deleteRoleFromUser(user, Role.USER)
            assertEquals(credentialsService.getUserRoles(user), listOf())
        }
    }

    @Test
    fun `i can get all credentials`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsWithRolesInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            assertEquals(credentialsService.getCredentials(), listOf(basicCredentialsWithRoles))
        }
    }

    @Test
    fun `i can delete credentials`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsWithRolesInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            credentialsService.deleteCredentials(user)
            assertEquals(credentialsInMemoryRepository.state().users, listOf())
        }
    }

    @Test
    fun `i can get activities from a role`() {
        runBlocking {
            val credentialsInMemoryRepository =
                CredentialsInMemoryRepository().withState(basicCredentialsWithRolesInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository().withState(basicRolesActivities)
            val credentialsService =
                CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)

            assertEquals(credentialsService.getRoleActivities(Role.USER), setOf(basicActivity))
        }
    }
}