package com.kos.common

import com.kos.common.Retry.retryWithExponentialBackoff
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RetryTest {

    @Test
    fun `retryWithExponentialBackoff should return result on successful attempt`() = runBlocking {
        val result = retryWithExponentialBackoff(5) {
            "Success"
        }

        assertEquals("Success", result)
    }

    @Test
    fun `retryWithExponentialBackoff should throw exception after maxAttempts unsuccessful attempts`() = runBlocking {
        assertIs<RuntimeException>(retryWithExponentialBackoff(3) {
            throw RuntimeException("Simulated error")
        })
    }

    @Test
    fun `retryWithExponentialBackoff should succeed after one unsuccessful attempt`() = runBlocking {
        val result = retryWithExponentialBackoff(2) {
            if (it == 0) {
                throw RuntimeException("Simulated error")
            } else {
                "Success"
            }
        }

        assertEquals("Success", result)
    }
}
