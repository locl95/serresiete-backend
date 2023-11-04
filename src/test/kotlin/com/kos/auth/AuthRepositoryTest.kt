package com.kos.auth

import kotlin.test.Test


interface AuthRepositoryTest {
    @Test
    fun ICanValidateCredentials(): Unit
    @Test
    fun ICanValidateToken(): Unit
    @Test
    fun ICanValidateExpiredToken(): Unit
    @Test
    fun ICanValidatePersistentToken(): Unit
    @Test
    fun ICanLogin(): Unit
    @Test
    fun ICanLogout(): Unit
}