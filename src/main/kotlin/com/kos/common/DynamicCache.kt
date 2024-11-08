package com.kos.common

import arrow.atomic.Atomic
import arrow.atomic.update
import arrow.atomic.value
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DynamicCache<T> : WithLogger("dynamicCache") {

    //TODO: Add size limit. We don't want to fuck up heap because we stored millions of values.
    private val matchCache: MutableMap<String, T> = mutableMapOf()
    private val mutex = Mutex()
    private val hits = Atomic<Int>(0)
    private val miss = Atomic<Int>(0)

    val hitRate: Double
        get() = if (hits.value + miss.value == 0) 0.0 else hits.value.toDouble() / (hits.value + miss.value)

    suspend fun get(id: String, fetch: suspend () -> T): T = mutex.withLock {
        matchCache[id].fold(
            {
                logger.debug("no hit in cache for $id")
                val res = fetch()
                matchCache[id] = res
                miss.update { it + 1 }
                res
            }, { res ->
                logger.debug("hit in cache for $id")
                hits.update { it + 1 }
                res
            })
    }

}