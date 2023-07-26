package com.kos.views.repository

import arrow.core.Either
import com.kos.common.WithState
import com.kos.views.SimpleView
import com.kos.views.ViewNotFound
import com.kos.views.ViewResult
import com.kos.views.ViewSuccess

interface ViewsRepository : WithState<List<SimpleView>> {
    fun getOwnViews(owner: String): List<SimpleView>
    fun get(id: String): SimpleView?
    fun create(owner: String, characterIds: List<Long>): ViewSuccess
    fun edit(id:String, characters: List<Long>): Either<ViewNotFound, ViewSuccess>
}