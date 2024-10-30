package com.kos.auth

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.assertFalse
import com.kos.auth.repository.AuthInMemoryRepository
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
import com.kos.assertTrue
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import kotlin.test.assertEquals

class AuthControllerTest {
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
    private val authRepository = AuthInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        rolesActivitiesState: Map<Role, Set<Activity>>,
        authState: List<Authorization>
    ): AuthController {
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)
        val authRepositoryWithState = authRepository.withState(authState)

        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        val authService = AuthService(authRepositoryWithState, credentialsService)
        return AuthController(authService)
    }


    @BeforeTest
    fun beforeEach() {
        credentialsRepository.clear()
        rolesActivitiesRepository.clear()
        authRepository.clear()
    }

    @Test
    fun `i can login`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(role, setOf(Activities.login))),
                listOf()
            )
            val res = controller.login("owner").getOrNull()
            assertTrue(res?.accessToken?.isAccess)
            assertFalse(res?.refreshToken?.isAccess)
            assertTrue(res?.accessToken?.userName == "owner")
            assertTrue(res?.refreshToken?.userName == "owner")
            assertTrue(res?.accessToken?.token?.isNotEmpty())
            assertTrue(res?.refreshToken?.token?.isNotEmpty())
        }
    }

    @Test
    fun `i can logout`() {
        runBlocking {
            val controller = createController(
                basicCredentialsInitialState,
                mapOf(),
                listOf(basicAuthorization.copy(userName = "owner"))
            )
            assertTrue(controller.logout("owner", setOf(Activities.logout)).getOrNull())
        }
    }

    @Test
    fun `I can refresh tokens`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf()
            )

            val controller = createController(
                credentialsState,
                mapOf(),
                listOf(basicAuthorization.copy(userName = "owner").copy(isAccess = false))
            )

            val res = controller.refresh(basicAuthorization.token).getOrNull()
            assertTrue(res?.isAccess)
            assertEquals("owner", res?.userName)
            assertTrue(res?.token?.isNotEmpty())
        }
    }

}