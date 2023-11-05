package com.kos.auth

import arrow.core.Either
import arrow.core.contains
import com.kos.auth.repository.AuthInMemoryRepository
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.*

class AuthInMemoryRepositoryTest : AuthRepositoryTest {

    @Test
    override fun ICanValidateToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(Authorization("test", "test", OffsetDateTime.now(), OffsetDateTime.now().plusHours(24)))
            )
            assertTrue(authInMemoryRepository.validateToken("test").contains("test"))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }

    @Test
    override fun ICanValidateExpiredToken() {
        val validUntil = OffsetDateTime.now().minusHours(1)

        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(Authorization("test", "test", OffsetDateTime.now(), validUntil))
            )
            val tokenOrError = authInMemoryRepository.validateToken("test")
            assertEquals(tokenOrError, Either.Left(TokenExpired("test", validUntil)))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }

    override fun ICanValidatePersistentToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(Authorization("test", "test", OffsetDateTime.now(), null))
            )
            val tokenOrError = authInMemoryRepository.validateToken("test")
            assertEquals(tokenOrError, Either.Right("test"))
            assertEquals(1, authInMemoryRepository.state().size)
        }
    }

    @Test
    override fun ICanLogin() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository()
            val userName = authInMemoryRepository.insertToken("test").userName
            assertEquals("test", userName)
            val finalStateOfAuthorizations = authInMemoryRepository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, "test")
        }
    }

    @Test
    override fun ICanLogout() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(Authorization("test", "test", OffsetDateTime.now(), OffsetDateTime.now().plusHours(24)))
            )
            assertTrue(authInMemoryRepository.deleteToken("test"))
            assertTrue(authInMemoryRepository.state().isEmpty())
        }
    }

}