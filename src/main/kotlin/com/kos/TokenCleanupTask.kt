package com.kos

import com.kos.auth.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

data class TokenCleanupTask(val authService: AuthService, val coroutineScope: CoroutineScope) : Runnable {

    private val logger = LoggerFactory.getLogger(TokenCleanupTask::class.java)

    override fun run() {
        coroutineScope.launch {
            logger.info("Running token cleanup task")
            val deletedTokens = runBlocking { authService.deleteExpiredTokens() }
            logger.info("Deleted $deletedTokens expired tokens")
        }
    }
}