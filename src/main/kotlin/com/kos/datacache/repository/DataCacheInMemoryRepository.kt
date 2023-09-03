package com.kos.datacache.repository

import com.kos.datacache.DataCache

class DataCacheInMemoryRepository(initialState: List<DataCache> = listOf()): DataCacheRepository {
    private val cachedData: MutableList<DataCache> = mutableListOf()

    init {
        cachedData.addAll(initialState)
    }

    override suspend fun insert(dataCache: DataCache) = cachedData.add(dataCache)
    override suspend fun update(dataCache: DataCache): Boolean {
        cachedData.removeAt(cachedData.indexOfFirst { it.characterId == dataCache.characterId })
        return cachedData.add(dataCache)
    }

    override suspend fun get(characterId: Long): DataCache? = cachedData.find { it.characterId == characterId }
    override suspend fun state(): List<DataCache> = cachedData
}