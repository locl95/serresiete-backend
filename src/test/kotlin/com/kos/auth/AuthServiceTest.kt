package com.kos.auth

import arrow.core.Either
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.basicRefreshAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.common.isDefined
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.*

class AuthServiceTest {

    private suspend fun createService(authInitialState: List<Authorization>): AuthService {
        val authInMemoryRepository = AuthInMemoryRepository().withState(authInitialState)
        return AuthService(authInMemoryRepository)
    }

    @Test
    fun `i can validate tokens and return the username of the owner`() {
        runBlocking {
            val authService = createService(listOf(basicAuthorization))
            authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
                .onRight { assertEquals(it, basicAuthorization.userName) }
                .onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i can validate that a refresh token can't be used when requesting an access`() {
        runBlocking {
            val authService = createService(listOf(basicRefreshAuthorization))
            authService.validateTokenAndReturnUsername(basicRefreshAuthorization.token, isAccessRequest = true)
                .onRight { fail() }
                .onLeft {
                    assertTrue(it is TokenWrongMode)
                    assertEquals(basicRefreshAuthorization.token, it.token)
                }
        }
    }

    @Test
    fun `i can validate that any token will not work regardless of type if they expired`() {
        runBlocking {
            val validUntil = OffsetDateTime.now().minusHours(1)
            val authService = createService(listOf(basicAuthorization.copy(validUntil = validUntil)))
            authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
                .onRight { fail() }
                .onLeft {
                    assertTrue(it is TokenExpired)
                    assertEquals(token, it.token)
                    assertEquals(validUntil, it.validUntil)
                }
            authService.validateTokenAndReturnUsername(token, isAccessRequest = false)
                .onRight { fail() }
                .onLeft {
                    assertTrue(it is TokenWrongMode)
                    assertEquals(token, it.token)
                }
        }
    }

    @Test
    fun `i can validate that a persistent token works`() {
        runBlocking {
            val authService = createService(listOf(basicAuthorization.copy(validUntil = null)))
            authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
                .onRight { assertEquals(user, it) }
                .onLeft { fail() }
        }
    }

    @Test
    fun `i can validate that login creates and returns both access and refresh tokens`() {
        runBlocking {
            val authService = createService(listOf())
            val loginResponse = authService.login(user)
            assertTrue(loginResponse.accessToken.isDefined())
            assertTrue(loginResponse.refreshToken.isDefined())
        }
    }

    @Test
    fun `i can get an access token with a refresh token without needing to login`() {
        runBlocking {
            val authService = createService(listOf(basicAuthorization, basicRefreshAuthorization))
            authService.refresh("refresh")
                .onRight { assertEquals(user, it?.userName) }
                .onLeft { fail(it.toString()) }
        }
    }

    @Test
    fun `i cant create an access token with another access token`() {
        runBlocking {
            val authService = createService(listOf(basicAuthorization, basicRefreshAuthorization))
            authService.refresh(token)
                .onRight { fail() }
                .onLeft {
                    assertTrue(it is TokenWrongMode)
                    assertEquals(token, it.token)
                }
        }
    }

    @Test
    fun `i can logout`() {
        runBlocking {
            val authService = createService(listOf(basicAuthorization, basicRefreshAuthorization))
            assertTrue(authService.logout(basicAuthorization.userName))
        }
    }
}