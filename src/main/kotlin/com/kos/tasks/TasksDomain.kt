package com.kos.tasks

import com.kos.auth.OffsetDateTimeSerializer
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.util.*

@Serializable
data class TaskStatus(val status: Status, val message: String?)

@Serializable
data class Task(
    val id: String,
    val type: TaskType,
    val taskStatus: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val inserted: OffsetDateTime
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun apply(id: String, type: TaskType, taskStatus: TaskStatus, inserted: OffsetDateTime): Task {
            return Task(id, type, json.encodeToString(taskStatus), inserted)
        }
    }
}


@Serializable
enum class Status {
    SUCCESSFUL {
        override fun toString(): String = "successful"
    },
    ERROR {
        override fun toString(): String = "error"
    };

    companion object {
        fun fromString(value: String): Status = when (value) {
            "successful" -> SUCCESSFUL
            "error" -> ERROR
            else -> throw IllegalArgumentException("Unknown status: $value")
        }
    }
}

@Serializable
enum class TaskType {
    CACHE_WOW_DATA_TASK {
        override fun toString(): String = "cacheWowDataTask"
    },
    CACHE_LOL_DATA_TASK {
        override fun toString(): String = "cacheLolDataTask"
    },
    TOKEN_CLEANUP_TASK {
        override fun toString(): String = "tokenCleanupTask"
    },
    TASK_CLEANUP_TASK {
        override fun toString(): String = "taskCleanupTask"
    };

    companion object {
        fun fromString(value: String): TaskType = when (value) {
            "cacheWowDataTask" -> CACHE_WOW_DATA_TASK
            "cacheLolDataTask" -> CACHE_LOL_DATA_TASK
            "tokenCleanupTask" -> TOKEN_CLEANUP_TASK
            "taskCleanupTask" -> TASK_CLEANUP_TASK
            else -> throw IllegalArgumentException("Unknown task: $value")
        }
    }
}

@Serializable
data class TaskRequest(val type: TaskType)