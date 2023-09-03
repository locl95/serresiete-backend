package com.kos.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.sql.DataSource

object DatabaseFactory {

    private val url = System.getenv("POSTGRES_URL") ?: "jdbc:h2:file:./build/db"
    private val user = System.getenv("POSTGRES_USER") ?: ""
    private val password = System.getenv("POSTGRES_PASSWORD") ?: ""
    private val driver = System.getenv("POSTGRES_DRIVER") ?: "org.h2.Driver"

    fun init(mustClean: Boolean) {

        fun hikari(): DataSource {
            val config = HikariConfig()
            config.driverClassName = driver
            config.jdbcUrl = url
            config.username = user
            config.password = password
            config.maximumPoolSize = 3
            config.isAutoCommit = false
            config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            config.validate()
            return HikariDataSource(config)
        }

        val flyway = Flyway
            .configure()
            .dataSource(url, user, password)
            .cleanDisabled(false)
            .load()

        Database.connect(hikari())
        if (mustClean) flyway.clean()
        flyway.migrate()
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}