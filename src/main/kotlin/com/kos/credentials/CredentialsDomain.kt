package com.kos.credentials

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val userName: String, val password: String)