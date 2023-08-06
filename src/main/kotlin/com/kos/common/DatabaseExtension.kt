package com.kos.common

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.PreparedStatement

fun <T : Table> T.insertOnConflictIgnore() {
    TransactionManager.current()
}