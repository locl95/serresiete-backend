package com.kos.auth

import com.kos.auth.AuthTestHelper.basicAuthorization
import com.kos.auth.AuthTestHelper.token
import com.kos.auth.AuthTestHelper.user
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
                authInMemoryRepository.getAuthorization(token),
                basicAuthorization
            )
        }
    }

    override fun ICanInsertAuthorizations() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository()
            val userName = authInMemoryRepository.insertToken(user).userName
            assertEquals(user, userName)
            val finalStateOfAuthorizations = authInMemoryRepository.state()
            assertContains(finalStateOfAuthorizations.map { it.userName }, user)
        }
    }

    override fun ICanDeleteAuthorizations() {
        runBlocking {
            val authInMemoryRepository = AuthInMemoryRepository(
                listOf(basicAuthorization)
            )
            assertTrue(authInMemoryRepository.deleteToken(token))
            assertTrue(authInMemoryRepository.state().isEmpty())
        }
    }

}