package com.kos.tasks

import kotlin.test.Test
import kotlin.test.assertEquals

class TasksDomainTest {

    @Test
    fun `entries to string should return a list of my task types as strings`() {
        val expected = listOf(
            "cacheWowDataTask",
            "cacheLolDataTask",
            "tokenCleanupTask",
            "taskCleanupTask"
        )
        assertEquals(expected, TaskType.entries.map { it.toString() })
    }
}