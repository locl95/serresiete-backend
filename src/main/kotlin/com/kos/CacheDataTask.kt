package com.kos

import com.kos.auth.AuthService
import com.kos.characters.CharactersService
import com.kos.characters.WowCharacter
import com.kos.datacache.DataCacheService
import com.kos.views.Game
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
            val wowCharacters = charactersService.get(Game.WOW)
            val lolCharacters = charactersService.get(Game.LOL)
            dataCacheService.cache(wowCharacters, Game.WOW)
            dataCacheService.cache(lolCharacters, Game.LOL)
            val deletedRecords = dataCacheService.clear()
            logger.info("Deleted $deletedRecords cached records")
        }

    }
}