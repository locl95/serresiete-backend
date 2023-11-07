package com.kos.credentials

import com.kos.credentials.repository.CredentialsInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CredentialsInMemoryRepositoryTest : CredentialsRepositoryTest {
    @Test
    override fun ICanGetCredentials() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository(listOf(Credentials("test", "test")))
            assertEquals(credentialsInMemoryRepository.getCredentials("test"), Credentials("test", "test"))
        }
    }

    @Test
    override fun ICanInsertCredentials() {
        runBlocking {
            val repository = CredentialsInMemoryRepository()
            assertTrue(repository.state().isEmpty())
            repository.insertCredentials(Credentials("test", "test"))
            assertTrue(repository.state().size == 1)
            assertTrue(repository.state().all { it.userName == "test" && it.password == "test" } )
        }
    }

    @Test
    override fun ICanModifyMyPassword() {
        runBlocking {
            val repository = CredentialsInMemoryRepository(listOf(Credentials("test", "test")))
            assertTrue(repository.state().size == 1)
            repository.editCredentials("test", "newPasswd")
            assertTrue(repository.state().all { it.userName == "test" && it.password == "newPasswd" } )
        }
    }

}