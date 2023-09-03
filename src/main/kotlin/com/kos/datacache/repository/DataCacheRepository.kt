package com.kos.datacache.repository

import com.kos.common.WithState
import com.kos.datacache.DataCache

interface DataCacheRepository : WithState<List<DataCache>> {
    suspend fun insert(dataCache: DataCache): Boolean
    suspend fun update(dataCache: DataCache): Boolean
    suspend fun get(characterId: Long): DataCache?
}