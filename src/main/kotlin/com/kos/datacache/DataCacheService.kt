package com.kos.datacache

import arrow.core.Either
import arrow.core.sequence
import com.kos.common.JsonParseError
import com.kos.datacache.repository.DataCacheRepository
import com.kos.raiderio.RaiderIoData
import com.kos.views.SimpleView
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime

data class DataCacheService(private val dataCacheRepository: DataCacheRepository) {

    private val ttl: Long = 24
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun insert(dataCache: DataCache): Boolean =
        when(val dc = dataCacheRepository.get(dataCache.characterId)) {
            null -> dataCacheRepository.insert(dataCache)
            else -> {
                if(OffsetDateTime.now().minusHours(ttl) >= dc.inserted) dataCacheRepository.update(dataCache)
                else false
            }
    }

    suspend fun get(characterId: Long) = dataCacheRepository.get(characterId)
    suspend fun getData(simpleView: SimpleView):Either<JsonParseError, List<RaiderIoData>>  {
        return simpleView.characterIds.mapNotNull {
            when(val data = dataCacheRepository.get(it)) {
                null -> null
                else -> {
                    try { Either.Right(json.decodeFromString<RaiderIoData>(data.data)) }
                    catch (se: SerializationException) {
                        Either.Left(JsonParseError(data.data, ""))
                    }
                    catch (iae: IllegalArgumentException ) {
                        Either.Left(JsonParseError(data.data, ""))
                    }
                }
            }
        }.sequence()
    }
}