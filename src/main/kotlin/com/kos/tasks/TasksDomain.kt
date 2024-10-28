package com.kos.tasks

import arrow.core.Either
import com.kos.auth.OffsetDateTimeSerializer
import com.kos.common.ControllerError
import com.kos.common.InvalidTaskType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime

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
        fun fromString(value: String): Either<InvalidTaskType, TaskType> = when (value) {
            "cacheWowDataTask" -> Either.Right(CACHE_WOW_DATA_TASK)
            "cacheLolDataTask" -> Either.Right(CACHE_LOL_DATA_TASK)
            "tokenCleanupTask" -> Either.Right(TOKEN_CLEANUP_TASK)
            "taskCleanupTask" -> Either.Right(TASK_CLEANUP_TASK)
            else -> Either.Left(InvalidTaskType(value))
        }
    }
}

@Serializable
data class TaskRequest(val type: TaskType)