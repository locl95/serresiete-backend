package com.kos.credentials

import com.kos.credentials.repository.CredentialsRepositoryState
import org.mindrot.jbcrypt.BCrypt

object CredentialsTestHelper {
    val user = "user"
    val password = "password"
    val basicCredentials = Credentials(user, password)
    val encryptedCredentials = Credentials(user, BCrypt.hashpw(password, BCrypt.gensalt(12)))
    val basicCredentialsInitialState = CredentialsRepositoryState(listOf(encryptedCredentials), mapOf())
}