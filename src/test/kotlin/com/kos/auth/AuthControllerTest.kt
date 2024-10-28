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
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import com.kos.assertTrue
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.views.ViewsTestHelper.owner
import kotlin.test.*

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

        val authService = AuthService(authRepositoryWithState)
        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        return AuthController(authService, credentialsService)
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
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.login))),
                listOf()
            )
            controller.login("owner").onRight {
                assertTrue(it.accessToken?.isAccess)
                assertFalse(it.refreshToken?.isAccess)
                assertEquals(owner, it.accessToken?.userName)
                assertEquals(owner, it.refreshToken?.userName)
                assertTrue(it.accessToken?.token?.isNotEmpty())
                assertTrue(it.refreshToken?.token?.isNotEmpty())
            }.onLeft {
                fail(it.toString())
            }

        }
    }

    @Test
    fun `i can logout`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(
                credentialsState,
                mapOf(Pair(Role.USER, setOf(Activities.logout))),
                listOf(basicAuthorization.copy(userName = "owner"))
            )
            controller.logout("owner").onRight {
                assertTrue(it)
            }.onLeft {
                fail(it.toString())
            }
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

            controller.refresh(basicAuthorization.token)
                .onRight {
                    assertTrue(it?.isAccess)
                    assertEquals(owner, it?.userName)
                    assertTrue(it?.token?.isNotEmpty())
                }
                .onLeft { fail(it.toString()) }
        }
    }

}