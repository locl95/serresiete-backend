package com.kos.credentials

import com.kos.credentials.repository.CredentialsRepositoryState

object CredentialsTestHelper {
    val user = "user"
    val password = "password"
    val basicCredentials = Credentials(user, password)
    val basicCredentialsInitialState = CredentialsRepositoryState(listOf(basicCredentials), mapOf(), mapOf())

}