package com.kos.characters

import com.kos.characters.repository.CharactersState
import com.kos.httpclients.domain.GetPUUIDResponse
import com.kos.httpclients.domain.GetSummonerResponse

object CharactersTestHelper {
    val emptyCharactersState = CharactersState(listOf(), listOf())
    val basicWowRequest = WowCharacterRequest("kakarona", "eu", "zuljin")
    val basicWowRequest2 = WowCharacterRequest("layser", "eu", "zuljin")
    val basicLolCharacter = LolCharacter(1, "GTP ZeroMVPs", "WOW", "1", 1, "1", 1)
    val basicLolCharacterEnrichedRequest = LolCharacterEnrichedRequest(
        basicLolCharacter.name,
        basicLolCharacter.tag,
        basicLolCharacter.puuid,
        basicLolCharacter.summonerIcon,
        basicLolCharacter.summonerId,
        basicLolCharacter.summonerLevel
    )
    val basicWowCharacter = basicWowRequest.toCharacter(1)
    val basicWowCharacter2 = basicWowRequest2.toCharacter(2)
    val basicGetSummonerResponse = GetSummonerResponse(
        "1",
        "1",
        "1",
        2,
        25,
        29
    )
    val basicGetPuuidResponse = GetPUUIDResponse(
        "1",
        "R7 Disney Girl",
        "EUW"
    )
}