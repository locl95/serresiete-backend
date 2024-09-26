package com.kos.riot

import arrow.core.Either
import com.kos.common.HttpError

interface RiotClient {
    suspend fun getPUUIDByRiotId(riotName: String, riotTag: String): Either<HttpError, GetPUUIDResponse>
    suspend fun getSummonerByPuuid(puuid: String): Either<HttpError, GetSummonerResponse>
    suspend fun getMatchesByPuuid(puuid: String): Either<HttpError, List<String>>
    suspend fun getMatchById(matchId: String): Either<HttpError, GetMatchResponse>
    suspend fun getLeagueEntriesBySummonerId(summonerId: String): Either<HttpError, List<LeagueEntryResponse>>
}