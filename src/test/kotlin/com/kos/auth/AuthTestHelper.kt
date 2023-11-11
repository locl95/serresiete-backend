package com.kos.auth

import java.time.OffsetDateTime

object AuthTestHelper {
    val user = "user"
    val token = "token"
    val basicAuthorization =
        Authorization(user, token, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), isAccess = true)
    val basicRefreshAuthorization = basicAuthorization.copy(
        token = "refresh",
        validUntil = basicAuthorization.validUntil?.plusDays(30),
        isAccess = false
    )
}