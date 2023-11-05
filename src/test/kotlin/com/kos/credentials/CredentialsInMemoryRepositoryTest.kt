package com.kos.credentials

import com.kos.credentials.repository.CredentialsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CredentialsInMemoryRepositoryTest : CredentialsRepositoryTest {
    @Test
    override fun ICanValidateCredentials() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository(listOf(User("test", "test")))
            assertTrue(credentialsInMemoryRepository.validateCredentials("test", "test"))
        }
    }

}