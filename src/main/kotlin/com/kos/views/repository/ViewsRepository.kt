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
    suspend fun create(owner: String, characterIds: List<Long>): ViewSuccess
    suspend fun edit(id:String, characters: List<Long>): ViewSuccess
}