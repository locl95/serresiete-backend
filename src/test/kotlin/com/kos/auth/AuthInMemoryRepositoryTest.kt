package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.repository.AuthInMemoryRepository
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthInMemoryRepositoryTest : AuthRepositoryTest {
    @Test
    override fun ICanGetAuthorizations() {
        val authInMemoryRepository = AuthInMemoryRepository(listOf(basicAuthorization))
        runBlocking {
            assertEquals(
                authInMemoryRepository.getAuthorization(basicAuthorization.token),
                basicAuthorization
            )
        }
    }

    override fun ICanInsertAuthorizations() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository()
            val userName = authInMemoryRepository.insertToken("test").userName
            assertEquals("test", userName)
            val finalStateOfAuthorizations = authInMemoryRepository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, "test")
        }
    }

    override fun ICanDeleteAuthorizations() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(basicAuthorization)
            )
            assertTrue(authInMemoryRepository.deleteToken("test"))
            assertTrue(authInMemoryRepository.state().isEmpty())
        }
    }

}