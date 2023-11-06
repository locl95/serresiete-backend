package com.kos.credentials

import com.kos.credentials.repository.CredentialsDatabaseRepository
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest {
    @Test
    override fun ICanGetCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(listOf(Credentials("test", "test")))
            assertEquals(repository.getCredentials("test"), Credentials("test", "test"))
        }
    }
}