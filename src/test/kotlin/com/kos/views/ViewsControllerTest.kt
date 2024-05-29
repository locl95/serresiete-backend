package com.kos.views

import com.kos.activities.Activities
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.raiderio.RaiderIoClient
import com.kos.roles.RolesTestHelper.role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.views.ViewsTestHelper.basicSimpleView
import com.kos.views.repository.ViewsInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsControllerTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)

    @Test
    fun `i can get views`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(listOf(basicCredentials.copy(userName = "owner")), mapOf(Pair("owner", listOf(role))))
            val viewsRepository = ViewsInMemoryRepository().withState(listOf(basicSimpleView))
            val charactersRepository = CharactersInMemoryRepository().withState(listOf())
            val charactersService = CharactersService(charactersRepository, raiderIoClient)
            val dataCacheRepository = DataCacheInMemoryRepository().withState(listOf())
            val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient)
            val service = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient)
            val credentialsRepository = CredentialsInMemoryRepository().withState(credentialsState)
            val rolesActivitiesRepository = RolesActivitiesInMemoryRepository().withState(mapOf(Pair(role, listOf(
                Activities.getOwnViews))))
            val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
            val controller = ViewsController(service, credentialsService)
            assertEquals(listOf(basicSimpleView), controller.getViews("owner").getOrNull())
        }
    }

    // Probar que si no se pasa un owner, devuelve Unauthorized

    // Probar que si faltan permisos, devuelve NotEnoughPermissions

    // Probar que si hay multiples vistas, un usuario al pedir las vistas, solo devuelve las suyas
    // Y un rol con actividad de getAllViews recibe todas


}