package com.kos.views.repository

import com.kos.common.Repository
import com.kos.common.WithState
import com.kos.views.SimpleView
import com.kos.views.ViewSuccess

interface ViewsRepository : Repository, WithState<List<SimpleView>> {
    suspend fun getOwnViews(owner: String): List<SimpleView>
    suspend fun get(id: String): SimpleView?
    suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewSuccess
    suspend fun edit(id:String, name: String, characters: List<Long>): ViewSuccess
    suspend fun delete(id: String): ViewSuccess
    suspend fun getViews(): List<SimpleView>
}