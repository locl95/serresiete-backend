package com.kos.tasks.repository

import com.kos.common.InMemoryRepository
import com.kos.tasks.Task
import com.kos.tasks.TaskType
import java.time.OffsetDateTime

class TasksInMemoryRepository : TasksRepository, InMemoryRepository {
    private val tasks: MutableList<Task> = mutableListOf()

    override suspend fun insertTask(task: Task) {
        tasks.add(task)
    }

    override suspend fun get(): List<Task> {
        return tasks.toList()
    }

    override suspend fun get(id: String): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun deleteOldTasks(olderThanDays: Long): Int {
        val daysAgo = OffsetDateTime.now().minusDays(olderThanDays)
        val deletedTasks = tasks.count { it.inserted < daysAgo }

        tasks.removeAll { it.inserted < daysAgo }

        return deletedTasks
    }

    override suspend fun getLastExecution(taskType: TaskType): Task? =
        tasks.toList().sortedByDescending { it.inserted }.firstOrNull { it.type == taskType }

    override suspend fun state(): List<Task> = tasks

    override suspend fun withState(initialState: List<Task>): TasksRepository {
        tasks.addAll(initialState)
        return this
    }

    override fun clear() {
        tasks.clear()
    }
}