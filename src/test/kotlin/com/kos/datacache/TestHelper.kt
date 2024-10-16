package com.kos.datacache

import com.kos.characters.CharactersTestHelper.basicLolCharacter
import java.time.OffsetDateTime

object TestHelper {

    private val wowData = """{                                
     "id": 1,                    
     "name": "Proassassin",       
     "spec": "Havoc",             
     "class": "Demon Hunter",     
     "score": 0.0,                
     "quantile": 0.0,             
     "mythicPlusRanks": {         
         "class": {               
             "realm": 0,          
             "world": 0,          
             "region": 0          
         },                       
         "specs": [],                       
         "overall": {             
             "realm": 0,          
             "world": 0,          
             "region": 0          
         }                        
     },                           
     "mythicPlusBestRuns": [],
     "type": "com.kos.httpclients.domain.RaiderIoData"
   }"""

    val lolData = """ {
        "type": "com.kos.httpclients.domain.RiotData",
        "summonerIcon": 1389,
        "summonerLevel": 499,
        "summonerName": "${basicLolCharacter.name}",
        "leagues": {
            "RANKED_SOLO_5x5": {
                "mainRole": "SUPPORT",
                "tier": "GOLD",
                "rank": "I",
                "leaguePoints": 1,
                "gamesPlayed": 27,
                "winrate": 0.5925925925925926,
                "matches": [
                    {
                        "championId": 497,
                        "role": "SUPPORT",
                        "individualPosition": "UTILITY",
                        "lane": "BOTTOM",
                        "kills": 2,
                        "deaths": 7,
                        "assists": 15,
                        "assistMePings": 0,
                        "visionWardsBoughtInGame": 8,
                        "enemyMissingPings": 0,
                        "wardsPlaced": 47,
                        "gameDuration": 1883,
                        "totalTimeSpentDead": 174,
                        "win": true
                    },
                    {
                        "championId": 497,
                        "role": "SUPPORT",
                        "individualPosition": "UTILITY",
                        "lane": "NONE",
                        "kills": 0,
                        "deaths": 2,
                        "assists": 20,
                        "assistMePings": 0,
                        "visionWardsBoughtInGame": 5,
                        "enemyMissingPings": 2,
                        "wardsPlaced": 20,
                        "gameDuration": 1146,
                        "totalTimeSpentDead": 24,
                        "win": true
                    },
                    {
                        "championId": 12,
                        "role": "SUPPORT",
                        "individualPosition": "UTILITY",
                        "lane": "NONE",
                        "kills": 2,
                        "deaths": 2,
                        "assists": 4,
                        "assistMePings": 0,
                        "visionWardsBoughtInGame": 6,
                        "enemyMissingPings": 0,
                        "wardsPlaced": 11,
                        "gameDuration": 917,
                        "totalTimeSpentDead": 36,
                        "win": true
                    },
                    {
                        "championId": 497,
                        "role": "SUPPORT",
                        "individualPosition": "UTILITY",
                        "lane": "BOTTOM",
                        "kills": 2,
                        "deaths": 3,
                        "assists": 21,
                        "assistMePings": 0,
                        "visionWardsBoughtInGame": 16,
                        "enemyMissingPings": 0,
                        "wardsPlaced": 51,
                        "gameDuration": 1712,
                        "totalTimeSpentDead": 67,
                        "win": true
                    },
                    {
                        "championId": 235,
                        "role": "SUPPORT",
                        "individualPosition": "UTILITY",
                        "lane": "BOTTOM",
                        "kills": 0,
                        "deaths": 2,
                        "assists": 13,
                        "assistMePings": 0,
                        "visionWardsBoughtInGame": 11,
                        "enemyMissingPings": 0,
                        "wardsPlaced": 32,
                        "gameDuration": 1856,
                        "totalTimeSpentDead": 73,
                        "win": true
                    }
                ]
            }
        }
    }"""

    private val now = OffsetDateTime.now()
    val outdatedDataCache = DataCache(1, wowData, now.minusHours(25))
    val wowDataCache = DataCache(1, wowData, now)
    val lolDataCache = DataCache(2, lolData, now)
}