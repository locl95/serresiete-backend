package com.kos.common

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <T : Table> T.insertOnConflictIgnore() {
    TransactionManager.current()
}