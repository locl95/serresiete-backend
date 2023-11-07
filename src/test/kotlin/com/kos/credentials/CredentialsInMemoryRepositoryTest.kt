package com.kos.credentials

import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
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
            repository.insertCredentials(basicCredentials)
            assertTrue(repository.state().size == 1)
            assertTrue(repository.state().all { it.userName == user && it.password == password } )
        }
    }

    @Test
    override fun ICanEditCredentials() {
        runBlocking {
            val repository = CredentialsInMemoryRepository(listOf(basicCredentials))
            assertTrue(repository.state().size == 1)
            repository.editCredentials(user, "newPassword")
            assertTrue(repository.state().all { it.userName == user && it.password == "newPassword" } )
        }
    }

}