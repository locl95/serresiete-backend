package com.kos.views.repository

import arrow.core.Either
import com.kos.characters.Character
import com.kos.views.*
import java.util.UUID

class ViewsInMemoryRepository(initialState: List<SimpleView> = listOf()) : ViewsRepository {

    private val views: MutableList<SimpleView> = mutableListOf()

    init {
        this.views.addAll(initialState)
    }

    override fun getOwnViews(owner: String): List<SimpleView> = views.filter { it.owner == owner }

    override fun get(id: String): SimpleView? = views.find { it.id == id }

    override fun create(owner: String, characterIds: List<Long>): ViewResult {
        val id = UUID.randomUUID().toString()
        views.add(SimpleView(id, owner, characterIds))
        return ViewSuccess(id)
    }

    override fun edit(id: String, characterIds: List<Long>): Either<ViewNotFound, ViewSuccess> {
        return when (val index = views.indexOfFirst { it.id == id }) {
            -1 -> Either.Left(ViewNotFound(id))
            else -> {
                val oldView = views[index]
                views.removeAt(index)
                views.add(index, SimpleView(id, oldView.owner, characterIds))
                Either.Right(ViewSuccess(id))
            }
        }
    }

    override fun state(): List<SimpleView> {
        return views
    }
}