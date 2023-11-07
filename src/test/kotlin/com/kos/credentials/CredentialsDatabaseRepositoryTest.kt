package com.kos.credentials

import com.kos.common.DatabaseFactory
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
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
            val repository = CredentialsDatabaseRepository().withState(listOf(basicCredentials))
            assertEquals(repository.getCredentials(user), Credentials(user, password))
        }
    }

    @Test
    override fun ICanInsertCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository()
            assertTrue(repository.state().isEmpty())
            repository.insertCredentials(basicCredentials)
            assertTrue(repository.state().size == 1)
            assertTrue(repository.state().all { it.userName == user && it.password == password})
        }
    }

    @Test
    override fun ICanEditCredentials() {
        runBlocking {
            val repository = CredentialsDatabaseRepository().withState(listOf(basicCredentials))
            repository.editCredentials(user, "newPassword")
            assertTrue(repository.state().contains(Credentials(user ,"newPassword" )))
        }
    }
}