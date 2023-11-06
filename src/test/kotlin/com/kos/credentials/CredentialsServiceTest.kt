package com.kos.credentials

import com.kos.credentials.repository.CredentialsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CredentialsServiceTest {
    @Test
    fun ICanValidateCredentials() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository(listOf(Credentials("test", "test")))
            val credentialsService = CredentialsService(credentialsInMemoryRepository)
            assertTrue(credentialsService.validateCredentials(Credentials("test", "test")))
        }
    }
}