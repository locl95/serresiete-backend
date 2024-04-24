package com.kos.roles

import com.kos.activities.ActivitiesTestHelper.basicActivity
import com.kos.common.DatabaseFactory
import com.kos.roles.RolesTestHelper.basicRolesActivities
import com.kos.roles.RolesTestHelper.role
import com.kos.roles.repository.RolesActivitiesDatabaseRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesActivitiesRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class RolesActivitiesRepositoryTest {

    abstract val repository: RolesActivitiesRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given a repository with roles and activities i can retrieve activities from a given role`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicRolesActivities)
            assertEquals(repositoryWithState.getActivitiesFromRole(role), setOf(basicActivity))
        }
    }

    @Test
    fun `given a repository i can insert an activity to it`() {
        runBlocking {
            repository.insertActivityToRole(basicActivity, role)
            assertEquals(repository.state(), basicRolesActivities)
        }
    }

    @Test
    fun `given a repository i can insert activities to it`() {
        val anotherActivity = "another activity"
        runBlocking {
            repository.insertActivityToRole(basicActivity, role)
            repository.insertActivityToRole(anotherActivity, role)
            assertEquals(repository.state(), mapOf(Pair(role, listOf(anotherActivity, basicActivity))))
        }
    }

    @Test
    fun `given a repository with one role and 1 activity i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicRolesActivities)
            repositoryWithState.deleteActivityFromRole(basicActivity, role)
            assertEquals(listOf(), repositoryWithState.state()[role].orEmpty())
        }
    }
}

class RolesActivitiesInMemoryRepositoryTest : RolesActivitiesRepositoryTest() {
    override val repository = RolesActivitiesInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class RolesActivitiesDatabaseRepositoryTest : RolesActivitiesRepositoryTest() {
    override val repository: RolesActivitiesRepository = RolesActivitiesDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
