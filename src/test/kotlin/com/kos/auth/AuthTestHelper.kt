package com.kos.auth

import java.time.OffsetDateTime

object AuthTestHelper {
    val basicAuthorization = Authorization("test", "test", OffsetDateTime.now(), OffsetDateTime.now().plusHours(24))
}