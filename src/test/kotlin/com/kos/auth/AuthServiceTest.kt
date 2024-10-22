package com.kos.auth

import arrow.core.Either
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.basicRefreshAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.common.isDefined
import com.kos.credentials.CredentialsService
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AuthServiceTest {
    @Test
    fun `i can validate tokens and return the username of the owner`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository().withState(listOf(basicAuthorization))
            val authService = AuthService(authInMemoryRepository, credentialsService)

            assertTrue(authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
                .fold({ false }) { it == basicAuthorization.userName })
        }
    }

    @Test
    fun `i can validate that a refresh token can't be used when requesting an access`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository().withState(listOf(basicRefreshAuthorization))
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val validateTokenAndReturnUsername =
                authService.validateTokenAndReturnUsername(basicRefreshAuthorization.token, isAccessRequest = true)

            assertTrue(validateTokenAndReturnUsername.isLeft())
        }
    }

    @Test
    fun `i can validate that any token will not work regardless of type if they expired`() {
        val validUntil = OffsetDateTime.now().minusHours(1)
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository =
                AuthInMemoryRepository().withState(listOf(basicAuthorization.copy(validUntil = validUntil)))
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val userNameOrErrorAccess = authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
            val userNameOrErrorRefresh = authService.validateTokenAndReturnUsername(token, isAccessRequest = false)

            assertEquals(userNameOrErrorAccess, Either.Left(TokenExpired(token, validUntil)))
            assertEquals(userNameOrErrorRefresh, Either.Left(TokenWrongMode(token, isAccess = true)))
        }
    }

    @Test
    fun `i can validate that a persistent token works`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository =
                AuthInMemoryRepository().withState(listOf(basicAuthorization.copy(validUntil = null)))
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val userNameOrError = authService.validateTokenAndReturnUsername(token, isAccessRequest = true)

            assertEquals(userNameOrError, Either.Right(user))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }

    @Test
    fun `i can validate that login creates and returns both access and refresh tokens`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository()
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val loginResponse = authService.login(user)

            assertEquals(2, authInMemoryRepository.state().size)

            loginResponse.onRight {
                assertTrue(it.accessToken.isDefined())
                assertTrue(it.refreshToken.isDefined())
                assertTrue(authInMemoryRepository.state().contains(it.accessToken))
                assertTrue(authInMemoryRepository.state().contains(it.refreshToken))
            }.onLeft {
                fail(it.message)
            }
        }
    }

    @Test
    fun `i can get an access token with a refresh token without needing to login`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository().withState(
                listOf(basicAuthorization, basicRefreshAuthorization)
            )
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val newToken = authService.refresh("refresh")

            assertEquals(3, authInMemoryRepository.state().size)
            assertTrue(newToken.isRight { it != null && it.userName == user })
        }
    }

    @Test
    fun `i cant create an access token with another access token`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository().withState(
                listOf(basicAuthorization, basicRefreshAuthorization)
            )
            val authService = AuthService(authInMemoryRepository, credentialsService)
            val newToken = authService.refresh(token)

            assertEquals(2, authInMemoryRepository.state().size)
            assertTrue(newToken.isLeft())
        }
    }

    @Test
    fun `i can logout`() {
        runBlocking {
            val credentialsRepository = CredentialsInMemoryRepository()
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val authInMemoryRepository = AuthInMemoryRepository().withState(
                listOf(basicAuthorization, basicRefreshAuthorization)
            )
            val authService = AuthService(authInMemoryRepository, credentialsService)

            authService.logout(basicAuthorization.userName)
            assertEquals(0, authInMemoryRepository.state().size)
        }
    }
}