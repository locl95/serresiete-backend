package com.kos.tasks.repository

import com.kos.common.DatabaseFactory.dbQuery
import com.kos.tasks.Task
import com.kos.tasks.TaskType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.OffsetDateTime
import java.util.*

class TasksDatabaseRepository : TasksRepository {
    object Tasks : Table() {
        val id = text("id")
        val type = text("type")
        val taskStatus = text("status")
        val inserted = text("inserted")

        override val primaryKey = PrimaryKey(id)
    }

    private fun resultRowToTask(row: ResultRow) = Task(
        row[Tasks.id],
        TaskType.fromString(row[Tasks.type]),
        row[Tasks.taskStatus],
        OffsetDateTime.parse(row[Tasks.inserted])
    )

    override suspend fun insertTask(task: Task) {
        dbQuery {
            Tasks.insert {
                it[id] = task.id
                it[type] = task.type.toString()
                it[taskStatus] = task.taskStatus
                it[inserted] = task.inserted.toString()
            }
        }
    }

    override suspend fun get(): List<Task> {
        return dbQuery { Tasks.selectAll().map { resultRowToTask(it) } }
    }

    override suspend fun get(id: String): Task? {
        return dbQuery { Tasks.select { Tasks.id.eq(id) }.map { resultRowToTask(it) }.singleOrNull() }
    }

    override suspend fun deleteOldTasks(olderThanDays: Long): Int {
        return dbQuery {
            Tasks.deleteWhere { inserted.less(OffsetDateTime.now().minusDays(olderThanDays).toString()) }
        }
    }

    override suspend fun getLastExecution(taskType: TaskType): Task? {
        return dbQuery {
            Tasks.select { Tasks.type.eq(taskType.toString()) }.orderBy(Tasks.inserted, SortOrder.DESC).limit(1)
                .map { resultRowToTask(it) }.firstOrNull()
        }
    }

    override suspend fun state(): List<Task> {
        return dbQuery { Tasks.selectAll().map { resultRowToTask(it) } }
    }

    override suspend fun withState(initialState: List<Task>): TasksRepository {
        dbQuery {
            Tasks.batchInsert(initialState) {
                this[Tasks.id] = it.id
                this[Tasks.type] = it.type.toString()
                this[Tasks.taskStatus] = it.taskStatus
                this[Tasks.inserted] = it.inserted.toString()
            }
        }
        return this
    }

}