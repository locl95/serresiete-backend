package com.kos.datacache.repository

import com.kos.common.WithState
import com.kos.datacache.DataCache

interface DataCacheRepository : WithState<List<DataCache>, DataCacheRepository> {
    suspend fun insert(data: List<DataCache>): Boolean
    suspend fun update(dataCache: DataCache): Boolean
    suspend fun get(characterId: Long): List<DataCache>
    suspend fun deleteExpiredRecord(ttl: Long): Int
}