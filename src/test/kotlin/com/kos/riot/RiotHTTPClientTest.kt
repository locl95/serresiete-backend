package com.kos.riot

import com.kos.assertTrue
import com.kos.httpclients.riot.RiotHTTPClient
import com.kos.riot.RiotHTTPClientHelper.client
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

//TODO: Añadir test sobre las excepciones

class RiotHTTPClientTest {
    private val riotHTTPClient = RiotHTTPClient(client, "fake-key")

    @Test
    fun `dummy test to check everything is going under control`() {
        runBlocking {
            val res = riotHTTPClient.getPUUIDByRiotId("GTP ZeroMVPs", "WOW")
            assertTrue(res.isRight())
            println(res)
        }
    }

    @Test
    fun `dummy test to check everything is going under control 2`() {
        runBlocking {
            val res = riotHTTPClient.getSummonerByPuuid("vJre0esG5sIx3rvCAe-YVsDfqCIMV5b2P-61wrYZ4w-hs9u_Ek8dVlo-KLo-GNA4NumLV1YTNxeCmA")
            assertTrue(res.isRight())
            println(res)
        }
    }

    @Test
    fun `dummy test to check everything is going under control 3`() {
        runBlocking {
            val res = riotHTTPClient.getMatchesByPuuid("vJre0esG5sIx3rvCAe-YVsDfqCIMV5b2P-61wrYZ4w-hs9u_Ek8dVlo-KLo-GNA4NumLV1YTNxeCmA")
            assertTrue(res.isRight())
            println(res)
        }
    }

    @Test
    fun `dummy test to check everything is going under control 4`() {
        runBlocking {
            val res = riotHTTPClient.getMatchById("EUW1_7130322326")
            println(res)
            assertTrue(res.isRight())
        }
    }

    @Test
    fun `dummy test to check everything is going under control 5`() {
        runBlocking {
            val res = riotHTTPClient.getLeagueEntriesBySummonerId("XpUAakpMee4budbZ_KVchTTxwkN4OHgqjbYa0r4pXR_Ya6E")
            println(res)
            assertTrue(res.isRight())
        }
    }
}