package com.kos.common

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DynamicCache<T> {

    //TODO: Add size limit. We don't want to fuck up heap because we stored millions of values.
    private val matchCache: MutableMap<String, T> = mutableMapOf()
    private val mutex = Mutex()

    suspend fun get(id: String, fetch: suspend () -> T): T = mutex.withLock {
        matchCache[id].fold(
            {
                val res = fetch()
                matchCache[id] = res
                res
            }, { it })
    }

}