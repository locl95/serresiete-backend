package com.kos.datacache.repository

import com.kos.datacache.DataCache

class DataCacheInMemoryRepository : DataCacheRepository {
    private val cachedData: MutableList<DataCache> = mutableListOf()

    override suspend fun insert(dataCache: DataCache) = cachedData.add(dataCache)
    override suspend fun update(dataCache: DataCache): Boolean {
        cachedData.removeAt(cachedData.indexOfFirst { it.characterId == dataCache.characterId })
        return cachedData.add(dataCache)
    }

    override suspend fun get(characterId: Long): DataCache? = cachedData.find { it.characterId == characterId }
    override suspend fun state(): List<DataCache> = cachedData
    override suspend fun withState(initialState: List<DataCache>): DataCacheInMemoryRepository {
        cachedData.addAll(initialState)
        return this
    }
}