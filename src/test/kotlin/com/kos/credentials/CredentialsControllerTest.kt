package com.kos.credentials

import com.kos.activities.Activities
import com.kos.common.CantDeleteYourself
import com.kos.common.NotEnoughPermissions
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRoles
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
import com.kos.credentials.CredentialsTestHelper.emptyCredentialsState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.Role
import kotlinx.coroutines.runBlocking
import kotlin.test.*


class CredentialsControllerTest {
    private val credentialsRepository = CredentialsInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
    ): CredentialsController {
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)

        val credentialsService = CredentialsService(credentialsRepositoryWithState)
        return CredentialsController(credentialsService)
    }


    @BeforeTest
    fun beforeEach() {
        credentialsRepository.clear()
    }

    @Test
    fun `i can get credentials`() {
        runBlocking {

            val controller = createController(basicCredentialsWithRolesInitialState)

            assertEquals(
                listOf(basicCredentialsWithRoles),
                controller.getCredentials("owner", setOf(Activities.getAnyCredentials)).getOrNull()
            )
        }
    }

    @Test
    fun `i can get a credential`() {
        runBlocking {

            val controller = createController(basicCredentialsWithRolesInitialState)
            val expected = CredentialsWithRoles(user, listOf(Role.USER))
            controller.getCredential("owner", setOf(Activities.getAnyCredential), user)
                .onLeft { fail(it.toString()) }
                .onRight { assertEquals(expected, it) }
        }
    }

    @Test
    fun `i can patch a credential`() {
        runBlocking {
            val controller = createController(basicCredentialsWithRolesInitialState)
            controller.patchCredential("owner", setOf(Activities.patchCredentials), user, PatchCredentialRequest("new-password", null))
                .onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i can create credentials`() {
        runBlocking {
            val controller = createController(emptyCredentialsState)

            assertTrue(
                controller.createCredential(
                    "owner",
                    setOf(Activities.createCredentials),
                    CreateCredentialRequest(user, password, setOf())
                ).isRight()
            )
        }
    }

    @Test
    fun `i can edit credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(credentialsState)

            assertTrue(
                controller.editCredential(
                    "owner",
                    setOf(Activities.editCredentials),
                    "owner",
                    EditCredentialRequest("password", setOf())
                ).isRight()
            )
        }
    }

    @Test
    fun `i can delete credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf(Pair("owner", listOf(Role.USER)))
            )

            val controller = createController(credentialsState)

            assertTrue(controller.deleteCredential("owner", setOf(Activities.deleteCredentials), user).isRight())
        }
    }

    @Test
    fun `i cant remove my own credentials`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner"), basicCredentials),
                mapOf()
            )

            val controller = createController(credentialsState)

            controller.deleteCredential("owner", setOf(Activities.deleteCredentials), "owner")
                .onRight { fail("expected failure") }
                .onLeft { assertTrue(it is CantDeleteYourself) }
        }
    }

}