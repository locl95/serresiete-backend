package com.kos.datacache

import java.time.OffsetDateTime

data class DataCache(val characterId: Long, val data: String, val inserted: OffsetDateTime, val game: String)
