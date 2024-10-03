package com.kos.datacache

import arrow.core.Either
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.common.HttpError
import com.kos.httpclients.domain.GetMatchResponse
import com.kos.httpclients.domain.LeagueEntryResponse
import com.kos.httpclients.domain.MatchInfo
import com.kos.httpclients.domain.MatchParticipant

object RiotMockHelper {
    val leagueEntries: Either<HttpError, List<LeagueEntryResponse>> =
        Either.Right(
            listOf(
                LeagueEntryResponse(
                    "RANKED_FLEX_SR",
                    "GOLD",
                    "I",
                    1,
                    13,
                    14,
                    false
                ),
                LeagueEntryResponse(
                    "RANKED_SOLO_5x5",
                    "GOLD",
                    "I",
                    1,
                    13,
                    14,
                    false
                )
            )
        )
    val matchId = "EUW1_232424252"
    val matches = Either.Right(listOf(matchId))
    val match: Either<HttpError, GetMatchResponse> = Either.Right(
        GetMatchResponse(
            MatchInfo(
                1,
                "COMPLETED",
                1,
                listOf(
                    MatchParticipant(
                        1,
                        basicLolCharacter.puuid,
                        1,
                        1,
                        1,
                        "SUPPORT",
                        "SUPPORT",
                        "BOT",
                        1,
                        1,
                        1,
                        18,
                        1,
                        25,
                        250,
                        10000
                    )
                )
            )
        )
    )
}