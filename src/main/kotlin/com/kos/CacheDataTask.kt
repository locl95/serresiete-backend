package com.kos

import com.kos.auth.AuthService
import com.kos.characters.CharactersService
import com.kos.datacache.DataCacheService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

data class CacheDataTask(
    val dataCacheService: DataCacheService,
    val charactersService: CharactersService,
    val coroutineScope: CoroutineScope
) : Runnable {

    private val logger = LoggerFactory.getLogger(CacheDataTask::class.java)

    override fun run() {
        coroutineScope.launch {
            logger.info("Running filling cache data task")
            val characters = charactersService.get()
            dataCacheService.cache(characters)
            val deletedRecords = dataCacheService.clear()
            logger.info("Deleted $deletedRecords")
        }

    }
}