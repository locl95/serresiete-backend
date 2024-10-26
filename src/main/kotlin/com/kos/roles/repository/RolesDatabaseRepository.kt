package com.kos.roles.repository

import RolesRepository
import com.kos.activities.Activity
import com.kos.common.DatabaseFactory
import com.kos.roles.Role
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class RolesDatabaseRepository(private val db: Database) : RolesRepository {

    object Roles : Table("roles") {
        val role = varchar("role", 256)

        override val primaryKey = PrimaryKey(role)
    }

    private fun resultRowToActivity(row: ResultRow): Role = row[Roles.role]

    override suspend fun getRoles(): List<Role> {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Roles.selectAll().map { resultRowToActivity(it) }
        }
    }

    override suspend fun insertRole(role: Role) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            Roles.insert { it[Roles.role] = role }
        }
    }

    override suspend fun deleteRole(role: Role) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            Roles.deleteWhere { Roles.role.eq(role) }
        }
    }

    override suspend fun state(): List<Activity> {
        return getRoles()
    }

    override suspend fun withState(initialState: List<Role>): RolesRepository {
        newSuspendedTransaction(Dispatchers.IO, db) {
            Roles.batchInsert(initialState) {
                this[Roles.role] = it
            }
        }
        return this
    }

}