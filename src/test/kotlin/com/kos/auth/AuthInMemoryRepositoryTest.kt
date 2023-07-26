package com.kos.auth

import arrow.core.contains
import com.kos.auth.repository.AuthInMemoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.assertContains
import kotlin.test.assertTrue

class AuthInMemoryRepositoryTest : AuthRepositoryTest {
    @Test
    override fun ICanValidateCredentials() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(Pair(listOf(User("test", "test")), listOf()))
            assertTrue(authInMemoryRepository.validateCredentials("test", "test"))
        }
    }

    @Test
    override fun ICanValidateToken() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                Pair(
                    listOf(),
                    listOf(Authorization("test", "test", OffsetDateTime.now(), OffsetDateTime.now().plusHours(24)))
                )
            )
            assertTrue(authInMemoryRepository.validateToken("test").contains("test"))
            assertEquals(1, authInMemoryRepository.state().second.size)
        }
    }

    @Test
    override fun ICanLogin() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository()
            val userName = authInMemoryRepository.insertToken("test").userName
            assertEquals("test", userName)
            val finalStateOfAuthorizations = authInMemoryRepository.state().second
            assertContains(finalStateOfAuthorizations.map { it.userName }, "test")
        }
    }

    @Test
    override fun ICanLogout() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                Pair(
                    listOf(),
                    listOf(Authorization("test", "test", OffsetDateTime.now(), OffsetDateTime.now().plusHours(24)))
                )
            )
            assertTrue(authInMemoryRepository.deleteToken("test"))
            assertTrue(authInMemoryRepository.state().second.isEmpty())
        }
    }

}