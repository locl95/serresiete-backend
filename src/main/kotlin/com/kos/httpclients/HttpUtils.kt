package com.kos.httpclients

import com.kos.common.WithLogger
import kotlinx.coroutines.delay

object HttpUtils : WithLogger("Retry"){
    suspend fun <T> retryWithFixedDelay(
        retries: Int,
        delayTime: Long = 1200L,
        block: suspend () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            if (retries > 0) {
                logger.info("Retryies left $retries")
                delay(delayTime)
                retryWithFixedDelay(retries - 1, delayTime, block)
            } else throw e
        }
    }
}