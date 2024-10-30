package com.kos.tasks

import java.time.OffsetDateTime
import java.util.*


object TasksTestHelper {
    val task: (OffsetDateTime) -> Task = { now ->
        Task.apply(
            UUID.randomUUID().toString(),
            TaskType.CACHE_WOW_DATA_TASK,
            TaskStatus(Status.SUCCESSFUL, "message"),
            now
        )
    }

    val taskWithType: (OffsetDateTime, TaskType) -> Task = { now, taskType ->
        Task(
            UUID.randomUUID().toString(),
            taskType,
            TaskStatus(Status.SUCCESSFUL, "message").toString(),
            now
        )
    }
}


