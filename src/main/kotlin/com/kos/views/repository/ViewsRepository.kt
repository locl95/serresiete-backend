package com.kos.views.repository

import com.kos.common.WithState
import com.kos.views.SimpleView
import com.kos.views.ViewDeleted
import com.kos.views.ViewModified

interface ViewsRepository : WithState<List<SimpleView>, ViewsRepository> {
    suspend fun getOwnViews(owner: String): List<SimpleView>
    suspend fun get(id: String): SimpleView?
    suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewModified
    suspend fun edit(id: String, name: String, published: Boolean, characters: List<Long>): ViewModified
    suspend fun delete(id: String): ViewDeleted
    suspend fun getViews(): List<SimpleView>
}