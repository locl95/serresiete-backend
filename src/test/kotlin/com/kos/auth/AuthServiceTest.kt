package com.kos.auth

import arrow.core.Either
import arrow.core.contains
import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.repository.AuthInMemoryRepository
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthServiceTest {
    @Test
    fun ICanValidateToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(listOf(basicAuthorization))
            val authService = AuthService(authInMemoryRepository)
            assertTrue(authService.validateToken("test").contains(basicAuthorization.token))
        }
    }

    @Test
    fun ICanValidateExpiredToken() {
        val validUntil = OffsetDateTime.now().minusHours(1)
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(listOf(basicAuthorization.copy(validUntil = validUntil)))
            val authService = AuthService(authInMemoryRepository)
            val tokenOrError = authService.validateToken("test")
            assertEquals(tokenOrError, Either.Left(TokenExpired("test", validUntil)))
        }
    }

    @Test
    fun ICanValidatePersistentToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(listOf(basicAuthorization.copy(validUntil = null)))
            val authService = AuthService(authInMemoryRepository)
            val tokenOrError = authService.validateToken("test")
            assertEquals(tokenOrError, Either.Right("test"))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }
}