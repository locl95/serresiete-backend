package com.kos.credentials

import com.kos.common.DatabaseFactory
import com.kos.credentials.repository.CredentialsDatabaseRepository
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertTrue

class CredentialsDatabaseRepositoryTest : CredentialsRepositoryTest {

    @Before
    fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }

    @Test
    override fun ICanGetCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(listOf(Credentials("test", "test")))
            assertEquals(repository.getCredentials("test"), Credentials("test", "test"))
        }
    }

    @Test
    override fun ICanInsertCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository()
            //This should be 0 but currently we have a migration which adds a row
            assertTrue(repository.state().size == 1)
            repository.insertCredentials(Credentials("test", "test"))
            assertTrue(repository.state().size == 2)
            assertTrue(repository.state().contains(Credentials("test" ,"test" )))
        }
    }

    @Test
    override fun ICanEditCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(listOf(Credentials("test", "test")))
            repository.editCredentials("test", "newPasswd")
            assertTrue(repository.state().contains(Credentials("test" ,"newPasswd" )))
        }
    }
}