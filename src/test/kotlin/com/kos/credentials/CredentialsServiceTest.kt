package com.kos.credentials

import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.basicCredentialsInitialState
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class CredentialsServiceTest {
    @Test
    fun `i can validate credentials`() {
        runBlocking {
            val credentialsInMemoryRepository = CredentialsInMemoryRepository().withState(basicCredentialsInitialState)
            val rolesActivitiesInMemoryRepository = RolesActivitiesInMemoryRepository()
            val credentialsService = CredentialsService(credentialsInMemoryRepository, rolesActivitiesInMemoryRepository)
            assertTrue(credentialsService.validateCredentials(basicCredentials))
        }
    }
}