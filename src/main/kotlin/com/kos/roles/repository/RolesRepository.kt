package com.kos.roles.repository

import com.kos.common.WithState
import com.kos.roles.Role

interface RolesRepository : WithState<List<Role>, RolesRepository> {
    suspend fun getRoles(): List<Role>
}
