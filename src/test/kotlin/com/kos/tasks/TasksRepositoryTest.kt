package com.kos.tasks

import com.kos.tasks.repository.TasksInMemoryRepository
import com.kos.tasks.repository.TasksRepository
import com.kos.common.DatabaseFactory
import com.kos.tasks.TasksTestHelper.task
import com.kos.tasks.repository.TasksDatabaseRepository
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class TasksRepositoryTest {
    abstract val repository: TasksRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty repository I can insert a task`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val task = task(now)
            repository.insertTask(task)
            assertEquals(listOf(task), repository.state())
        }
    }

    @Test
    fun `given a repository with tasks I can retrieve the latest inserted`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val plusMinutes = now.plusMinutes(30)
            val expected = task(plusMinutes)
            val repositoryWithState = repository.withState(listOf(task(now), expected))
            val task = repositoryWithState.getLastExecution(taskType = TaskType.CACHE_WOW_DATA_TASK)
            assertEquals(expected, task)
        }
    }

    @Test
    fun `given a repository with tasks I can delete the old ones`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val oldTask = task(now.minusDays(8))
            val recentlyInsertedTask = task(now)
            val repositoryWithState = repository.withState(listOf(recentlyInsertedTask, oldTask))
            val deletedTasks = repositoryWithState.deleteOldTasks(7)
            assertEquals(1, deletedTasks)
            assertEquals(repositoryWithState.state(), listOf(recentlyInsertedTask))
        }
    }

    @Test
    fun `given a repository with tasks I can retrieve them`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val task = task(now)
            val repositoryWithState = repository.withState(listOf(task))
            assertEquals(listOf(task), repositoryWithState.get())
        }
    }

    @Test
    fun `given a repository with tasks I can retrieve them by id`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val knownId = "1"
            val task = task(now).copy(id = knownId)
            val repositoryWithState = repository.withState(listOf(task))
            assertEquals(task, repositoryWithState.get(knownId))
        }
    }
}

class TasksInMemoryRepositoryTest : TasksRepositoryTest() {
    override val repository = TasksInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class TasksDatabaseRepositoryTest : TasksRepositoryTest() {
    override val repository = TasksDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
