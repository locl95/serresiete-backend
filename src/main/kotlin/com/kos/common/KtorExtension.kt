package com.kos.common

import com.kos.credentials.Credentials
import io.ktor.server.auth.*

fun UserPasswordCredential.toCredentials() = Credentials(this.name, this.password)