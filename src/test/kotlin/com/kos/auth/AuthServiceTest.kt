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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthServiceTest {
    @Test
    fun ICanValidateAccessToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository().withState(listOf(basicAuthorization))
            val authService = AuthService(authInMemoryRepository)
            assertTrue(authService.validateTokenAndReturnUsername(token,isAccessRequest = true)
                .fold({ false }) { it == basicAuthorization.userName })
        }
    }

    @Test
    fun ICantValidateARefreshTokenWhenExpectingAccess() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository().withState(listOf(basicRefreshAuthorization))
            val authService = AuthService(authInMemoryRepository)
            val validateTokenAndReturnUsername =
                authService.validateTokenAndReturnUsername(basicRefreshAuthorization.token, isAccessRequest = true)
            assertTrue(validateTokenAndReturnUsername.isLeft())
        }
    }

    @Test
    fun ICantValidateExpiredTokenRegardlessOfRequestType() {
        val validUntil = OffsetDateTime.now().minusHours(1)
        runBlocking {
            val authInMemoryRepository =
                AuthInMemoryRepository().withState(listOf(basicAuthorization.copy(validUntil = validUntil)))
            val authService = AuthService(authInMemoryRepository)
            val userNameOrErrorAccess = authService.validateTokenAndReturnUsername(token, isAccessRequest=true)
            val userNameOrErrorRefresh = authService.validateTokenAndReturnUsername(token, isAccessRequest=false)
            assertEquals(userNameOrErrorAccess, Either.Left(TokenExpired(token, validUntil)))
            assertEquals(userNameOrErrorRefresh, Either.Left(TokenWrongMode(token, isAccess = true)))
        }
    }

    @Test
    fun ICanValidatePersistentAccessToken() {
        runBlocking {
            val authInMemoryRepository =
                AuthInMemoryRepository().withState(listOf(basicAuthorization.copy(validUntil = null)))
            val authService = AuthService(authInMemoryRepository)
            val userNameOrError = authService.validateTokenAndReturnUsername(token, isAccessRequest = true)
            assertEquals(userNameOrError, Either.Right(user))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }

    @Test
    fun ICanLogin() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository()
            val authService = AuthService(authInMemoryRepository)
            val loginResponse = authService.login(user)
            assertTrue(loginResponse.accessToken.isDefined())
            assertTrue(loginResponse.refreshToken.isDefined())
            assertEquals(2, authInMemoryRepository.state().size)
            assertTrue(authInMemoryRepository.state().contains(loginResponse.accessToken))
            assertTrue(authInMemoryRepository.state().contains(loginResponse.refreshToken))
        }
    }

    @Test
    fun ICanRefreshTokens() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository().withState(
                listOf(basicAuthorization, basicRefreshAuthorization)
            )
            val authService = AuthService(authInMemoryRepository)
            val newToken = authService.refresh("refresh")
            assertEquals(3, authInMemoryRepository.state().size)
            assertTrue(newToken.isRight { it != null && it.userName == user })
        }
    }

    @Test
    fun RefreshTokenFailsIfUsingAnAccessToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository().withState(
                listOf(basicAuthorization, basicRefreshAuthorization)
            )
            val authService = AuthService(authInMemoryRepository)
            val newToken = authService.refresh(token)
            assertEquals(2, authInMemoryRepository.state().size)
            assertTrue(newToken.isLeft())
        }
    }
}