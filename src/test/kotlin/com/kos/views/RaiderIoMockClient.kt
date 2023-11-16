package com.kos.views

import arrow.core.Either
import com.kos.characters.Character
import com.kos.common.JsonParseError
import com.kos.raiderio.*

class RaiderIoMockClient : RaiderIoClient {
    override suspend fun get(character: Character): Either<JsonParseError, RaiderIoResponse> {
        return Either.Right(
            RaiderIoResponse(
                RaiderIoProfile(
                    character.name,
                    "class",
                    "spec",
                    listOf(MythicPlusSeasonScore("df-3", SeasonScores(0.0, 0.0, 0.0, 0.0, 0.0))),
                    MythicPlusRanks(
                        MythicPlusRank(1, 1, 1),
                        MythicPlusRank(1, 1, 1)
                    ),
                    listOf(),
                    listOf(),
                ),
                listOf()
            )
        )
    }

    override suspend fun cutoff(): Either<JsonParseError, RaiderIoCutoff> {
        return Either.Right(RaiderIoCutoff(100))
    }
}