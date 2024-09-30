package com.kos.tasks.repository

import com.kos.common.WithState
import com.kos.tasks.Task

interface TasksRepository : WithState<List<Task>, TasksRepository> {
    suspend fun insertTask(task: Task): Unit
}
