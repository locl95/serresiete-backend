package com.kos.common

import arrow.core.Either
import kotlinx.coroutines.delay

object Retry : WithLogger("retry") {
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

    suspend fun <L, R> retryEitherWithExponentialBackoff(
        maxAttempts: Int,
        attempt: Int = 1,
        initialDelayMillis: Long = 100,
        factor: Double = 2.0,
        maxDelayMillis: Long = Long.MAX_VALUE,
        block: suspend () -> Either<L, R>
    ): Either<L, R> {
        return when (val res = block()) {
            is Either.Right -> res
            is Either.Left -> {
                if (attempt < maxAttempts) {
                    delay(initialDelayMillis)
                    val nextDelay = (initialDelayMillis * factor).coerceAtMost(maxDelayMillis.toDouble()).toLong()
                    logger.info("Retry number: $attempt, next delay: $nextDelay")
                    retryEitherWithExponentialBackoff(
                        maxAttempts,
                        attempt + 1,
                        nextDelay,
                        factor,
                        maxDelayMillis,
                        block
                    )
                } else res
            }
        }
    }
}