package com.kos.datacache

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.common.HttpError
import com.kos.clients.domain.*

object BlizzardMockHelper {
    fun getCharacterProfile(wowCharacter: WowCharacter): Either<HttpError, GetWowCharacterResponse> {
        return Either.Right(
            GetWowCharacterResponse(
                wowCharacter.id,
                wowCharacter.name,
                60,
                false,
                50,
                50,
                "Hunter",
                "Night Elf",
                wowCharacter.realm,
                "PERO LA QUERIA TANTO",
                0
            )
        )
    }

    fun cutoff(): Either<HttpError, RaiderIoCutoff> {
        return Either.Right(RaiderIoCutoff(100))
    }

    val raiderIoData = listOf(
        RaiderIoData(
            id = 1,
            name = "kakarona",
            score = 0.0,
            `class` = "class",
            spec = "spec",
            quantile = 1.0,
            mythicPlusRanks = MythicPlusRanksWithSpecs(
                overall = MythicPlusRank(world = 1, region = 1, realm = 1),
                `class` = MythicPlusRank(world = 1, region = 1, realm = 1),
                specs = listOf()
            ), mythicPlusBestRuns = listOf()
        )
    )

    val raiderIoDataString = """
        {
          "type": "com.kos.clients.domain.RaiderIoData",
          "id": 1,
          "name": "kakarona",
          "score": 0.0,
          "class": "class",
          "spec": "spec",
          "quantile": 1.0,
          "mythicPlusRanks": {
            "overall": {
              "world": 1,
              "region": 1,
              "realm": 1
            },
            "class": {
              "world": 1,
              "region": 1,
              "realm": 1
            },
            "specs": [
              
            ]
          },
          "mythicPlusBestRuns": [
            
          ]
        }
    """.trimIndent()

    val raiderioCachedData: RaiderIoData = RaiderIoData(
        id = 1,
        name = "Proassassin",
        score = 0.0,
        `class` = "Demon Hunter",
        spec = "Havoc",
        quantile = 0.0,
        mythicPlusRanks = MythicPlusRanksWithSpecs(
            overall = MythicPlusRank(world = 0, region = 0, realm = 0),
            `class` = MythicPlusRank(world = 0, region = 0, realm = 0),
            specs = listOf()
        ),
        mythicPlusBestRuns = listOf()
    )


}