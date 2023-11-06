package com.kos.credentials

import kotlin.test.Test

interface CredentialsRepositoryTest {
    @Test
    fun ICanGetCredentials(): Unit
    @Test
    fun ICanInsertCredentials(): Unit
}