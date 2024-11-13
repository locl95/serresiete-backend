package com.kos.credentials

import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.roles.Role
import org.mindrot.jbcrypt.BCrypt

object CredentialsTestHelper {
    val user = "user"
    val password = "password"
    val basicCredentials = Credentials(user, password)
    val basicCredentialsWithRoles = CredentialsWithRoles(user, listOf(Role.USER))
    val encryptedCredentials = Credentials(user, BCrypt.hashpw(password, BCrypt.gensalt(12)))
    val emptyCredentialsState = CredentialsRepositoryState(listOf(), mapOf())
    val basicCredentialsInitialState = CredentialsRepositoryState(listOf(encryptedCredentials), mapOf())
    val basicCredentialsWithRolesInitialState = CredentialsRepositoryState(
        listOf(encryptedCredentials), mapOf(Pair(user, listOf(Role.USER)))
    )
    val emptyCredentialsInitialState = CredentialsRepositoryState(listOf(), mapOf())
}