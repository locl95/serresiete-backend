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
}
