package com.kos.activities

import com.kos.activities.ActivitiesTestHelper.basicActivities
import com.kos.activities.ActivitiesTestHelper.basicActivity
import com.kos.activities.repository.ActivitiesDatabaseRepository
import com.kos.activities.repository.ActivitiesInMemoryRepository
import com.kos.activities.repository.ActivitiesRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.*

abstract class ActivitiesRepositoryTestCommon {

    abstract val repository: ActivitiesRepository
    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given a repository with activities i can retrieve them`() {
        runBlocking {
            val repositoryWithState = repository.withState(basicActivities)
            assertEquals(repositoryWithState.getActivities(), basicActivities)
        }
    }

    @Test
    fun `given an empty repository i can insert an activity`() {
        runBlocking {
            repository.insertActivity(basicActivity)
            val state = repository.state()
            assertContains(state, basicActivity)
        }
    }

    @Test
    fun `given a repository with one activity i can delete it`() {
        runBlocking {
            val repositoryWithState = repository.withState(listOf(basicActivity))
            repositoryWithState.deleteActivity(basicActivity)
            assertTrue(repositoryWithState.state().isEmpty())
        }
    }
}

class ActivitiesInMemoryRepositoryTest : ActivitiesRepositoryTestCommon() {
    override val repository = ActivitiesInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class ActivitiesDatabaseRepositoryTest : ActivitiesRepositoryTestCommon() {
    override val repository: ActivitiesRepository = ActivitiesDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
