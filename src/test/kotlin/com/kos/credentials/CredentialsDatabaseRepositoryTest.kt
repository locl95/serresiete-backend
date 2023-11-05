package com.kos.credentials

import com.kos.credentials.repository.CredentialsDatabaseRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest{
    @Test
    override fun ICanValidateCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(listOf(User("test", "test")))
            assertTrue(repository.validateCredentials("test", "test"))
        }
    }
}