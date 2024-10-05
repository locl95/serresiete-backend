package com.kos.tasks

import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class TaskStatus(val status: Status, val message: String)

data class Task(val type: TaskType, val taskStatus: String, val inserted: OffsetDateTime) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun apply(type: TaskType, taskStatus: TaskStatus, inserted: OffsetDateTime): Task {
            return Task(type, json.encodeToString(taskStatus), inserted)
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

enum class TaskType {
    CACHE_WOW_DATA_TASK {
        override fun toString(): String = "cacheWowDataTask"
    },
    CACHE_LOL_DATA_TASK {
        override fun toString(): String = "cacheLolDataTask"
    },
    TOKEN_CLEANUP_TASK {
        override fun toString(): String = "tokenCleanupTask"
    };

    companion object {
        fun fromString(value: String): TaskType = when (value) {
            "cacheWowDataTask" -> CACHE_WOW_DATA_TASK
            "cacheLolDataTask" -> CACHE_LOL_DATA_TASK
            "tokenCleanupTask" -> TOKEN_CLEANUP_TASK
            else -> throw IllegalArgumentException("Unknown task: $value")
        }
    }
}