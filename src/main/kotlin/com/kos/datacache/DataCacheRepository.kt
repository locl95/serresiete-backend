package com.kos.datacache

import com.kos.common.WithState

interface DataCacheRepository : WithState<List<DataCache>> {
    fun insert(dataCache: DataCache): Boolean
    fun update(dataCache: DataCache): Boolean
    fun get(characterId: Long): DataCache?
}