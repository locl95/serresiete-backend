package com.kos.auth

import java.time.OffsetDateTime

object AuthTestHelper {
    val user = "user"
    val token = "token"
    val basicAuthorization = Authorization(user, token, OffsetDateTime.now(), OffsetDateTime.now().plusHours(24))
}