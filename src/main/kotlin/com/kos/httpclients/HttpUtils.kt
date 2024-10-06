package com.kos.httpclients

import arrow.core.Either
import com.kos.common.WithLogger
import kotlinx.coroutines.delay

object HttpUtils : WithLogger("retry") {
    suspend fun <L, R> retryEitherWithFixedDelay(
        retries: Int,
        delayTime: Long = 1200L,
        functionName: String,
        block: suspend () -> Either<L, R>
    ): Either<L, R> {
        return when (val res = block()) {
            is Either.Right -> res
            is Either.Left ->
                if (retries > 0) {
                    logger.info("Retries left $retries for $functionName")
                    delay(delayTime)
                    retryEitherWithFixedDelay(retries - 1, delayTime, functionName, block)
                } else res
        }
    }
}