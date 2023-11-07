package com.kos.credentials.repository

import com.kos.common.WithState
import com.kos.credentials.Activity
import com.kos.credentials.Credentials

interface CredentialsRepository : WithState<List<Credentials>> {
    suspend fun getCredentials(userName: String): Credentials?
    suspend fun insertCredentials(credentials: Credentials): Unit
    suspend fun getActivities(user: String): List<Activity>
    suspend fun editCredentials(userName: String, newPassword: String) : Unit

}