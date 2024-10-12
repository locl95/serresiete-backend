package com.kos.tasks.repository

import com.kos.common.WithState
import com.kos.tasks.Task
import com.kos.tasks.TaskType

interface TasksRepository : WithState<List<Task>, TasksRepository> {
    suspend fun insertTask(task: Task): Unit
    suspend fun get(): List<Task>
    suspend fun get(id: String): Task?
    suspend fun deleteOldTasks(olderThanDays: Long): Int
    suspend fun getLastExecution(taskType: TaskType): Task?
}
