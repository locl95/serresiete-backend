package com.kos.views

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.common.HttpError
import com.kos.raiderio.*

object RaiderIoMockHelper {
    fun get(wowCharacter: WowCharacter): Either<HttpError, RaiderIoResponse> {
        return Either.Right(
            RaiderIoResponse(
                RaiderIoProfile(
                    wowCharacter.name,
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