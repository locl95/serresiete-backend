package com.kos.roles

import RolesRepository
import com.kos.activities.RolesTestHelper.role
import com.kos.common.DatabaseFactory
import com.kos.roles.repository.RolesDatabaseRepository
import com.kos.roles.repository.RolesInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*

abstract class RolesRepositoryTestCommon {

    abstract val repository: RolesRepository
    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given a repository with roles i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(role))
            assertEquals(repositoryWithState.getRoles(), listOf(role))
        }
    }

    @Test
    fun `given an empty repository i can insert a role`() {
        runBlocking {
            repository.insertRole(role)
            val state = repository.state()
            assertContains(state, role)
        }
    }

    @Test
    fun `given a repository with one role i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(role))
            repositoryWithState.deleteRole(role)
            assertTrue(repositoryWithState.state().isEmpty())
        }
    }
}

class RolesInMemoryRepositoryTest : RolesRepositoryTestCommon() {
    override val repository = RolesInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class RolesDatabaseRepositoryTest : RolesRepositoryTestCommon() {
    override val repository: RolesRepository = RolesDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
