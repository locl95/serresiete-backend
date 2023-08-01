package com.kos.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.print.attribute.standard.JobOriginatingUserName
import javax.sql.DataSource

object DatabaseFactory {

    //private val dbUrl: String = TODO("appConfig.property(\"db.jdbcUrl\").getString()")
    //private val dbUser: String = TODO("appConfig.property(\"db.dbUser\").getString()")
    //private val dbPassword: String = TODO("appConfig.property(\"db.dbPassword\").getString()")
    private fun hikari(driver: String, url: String, userName: String, password: String): DataSource {
        val config = HikariConfig()
        //TODO: POSTGRESQL DB config.driverClassName = "org.postgresql.Driver"
        config.driverClassName = driver
        config.jdbcUrl = url //TODO: dbUrl
        config.username = userName //TODO: dbUser
        config.password = password  //TODO:dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }


    fun init(driver: String, url: String, userName: String, password: String, mustClean: Boolean) {
        val flyway = Flyway
            .configure()
            .dataSource(url, userName, password)
            .cleanDisabled(false)
            .load()
        Database.connect(hikari(driver, url, userName, password))
        if (mustClean) flyway.clean()
        flyway.migrate()
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}