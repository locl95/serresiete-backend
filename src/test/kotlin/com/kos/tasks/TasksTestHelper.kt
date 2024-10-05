package com.kos.tasks

import java.time.OffsetDateTime


object TasksTestHelper {
    val task: (OffsetDateTime) -> Task = { now ->
        Task.apply(
            TaskType.CACHE_WOW_DATA_TASK,
            TaskStatus(Status.SUCCESSFUL, "message"),
            now
        )
    }
}
