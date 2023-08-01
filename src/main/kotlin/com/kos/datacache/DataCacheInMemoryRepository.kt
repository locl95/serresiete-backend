package com.kos.datacache

class DataCacheInMemoryRepository(initialState: List<DataCache> = listOf()): DataCacheRepository {
    private val cachedData: MutableList<DataCache> = mutableListOf()

    init {
        cachedData.addAll(initialState)
    }

    override fun insert(dataCache: DataCache) = cachedData.add(dataCache)
    override fun update(dataCache: DataCache): Boolean {
        cachedData.removeAt(cachedData.indexOfFirst { it.characterId == dataCache.characterId })
        return cachedData.add(dataCache)
    }

    override fun get(characterId: Long): DataCache? = cachedData.find { it.characterId == characterId }
    override suspend fun state(): List<DataCache> = cachedData
}