package com.kos.views

import arrow.core.Either
import com.kos.characters.Character
import com.kos.common.HttpError
import com.kos.raiderio.*

object RaiderIoMockHelper {
    fun get(character: Character): Either<HttpError, RaiderIoResponse> {
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
                    listOf()
                ),
                listOf()
            )
        )
    }

    fun cutoff(): Either<HttpError, RaiderIoCutoff> {
        return Either.Right(RaiderIoCutoff(100))
    }
}