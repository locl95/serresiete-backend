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
