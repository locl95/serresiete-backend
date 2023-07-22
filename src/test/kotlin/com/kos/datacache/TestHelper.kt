package com.kos.datacache

import java.time.OffsetDateTime

object TestHelper {
    private val now = OffsetDateTime.now()
    val outdatedDataCache = DataCache(1, "outdated_cache", now.minusHours(25))
    val dataCache = DataCache(1, "cache", now)
}