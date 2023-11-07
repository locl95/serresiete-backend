package com.kos.views.repository

import arrow.core.Either
import com.kos.common.WithState
import com.kos.views.SimpleView
import com.kos.views.ViewNotFound
import com.kos.views.ViewResult
import com.kos.views.ViewSuccess

interface ViewsRepository : WithState<List<SimpleView>> {
    suspend fun getOwnViews(owner: String): List<SimpleView>
    suspend fun get(id: String): SimpleView?
    suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewSuccess
    suspend fun edit(id:String, name: String, characters: List<Long>): ViewSuccess
    suspend fun delete(id: String): ViewSuccess
    suspend fun getViews(): List<SimpleView>
}