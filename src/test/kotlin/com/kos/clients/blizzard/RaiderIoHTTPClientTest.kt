package com.kos.clients.blizzard

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.clients.blizzard.RaiderioHttpClientHelper.getWowCharacterResponse
import com.kos.clients.domain.GetWowCharacterResponse
import com.kos.clients.domain.RaiderIoResponse
import com.kos.clients.raiderio.RaiderioHttpClientHelper.client
import com.kos.clients.raiderio.RaiderioHttpClientHelper.raiderioProfileResponse
import com.kos.common.HttpError
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class BlizzardHTTPClientTest {

    //TODO: Finish this suite that fails !!!!!

    private val blizzardAuthClient = mock(BlizzardAuthClient::class.java)
    private val blizzardClient = BlizzardHttpClient(client, blizzardAuthClient)

    //TODO: This suite must be more extensive

    @Test
    fun `test get() method with successful response`() {
        runBlocking {
            val result: Either<HttpError, GetWowCharacterResponse> = blizzardClient.getCharacterProfile(
                "region", "realm", "name"
            )
            assertEquals(Either.Right(getWowCharacterResponse), result)
        }
    }
}
