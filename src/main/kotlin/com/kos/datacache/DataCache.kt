package com.kos.datacache

import com.kos.views.Game
import java.time.OffsetDateTime

data class DataCache(val characterId: Long, val data: String, val inserted: OffsetDateTime, val game: Game)
