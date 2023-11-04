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

    override suspend fun getOwnViews(owner: String): List<SimpleView> = views.filter { it.owner == owner }

    override suspend fun get(id: String): SimpleView? = views.find { it.id == id }

    override suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewSuccess {
        val id = UUID.randomUUID().toString()
        views.add(SimpleView(id, name, owner, characterIds))
        return ViewSuccess(id)
    }

    override suspend fun edit(id: String, name: String, characters: List<Long>): ViewSuccess {
        val index = views.indexOfFirst { it.id == id }
        val oldView = views[index]
        views.removeAt(index)
        views.add(index, SimpleView(id, name, oldView.owner, characters))
        return ViewSuccess(id)
    }

    override suspend fun delete(id: String): ViewSuccess {
        val index = views.indexOfFirst { it.id == id }
        views.removeAt(index)
        return ViewSuccess(id)
    }

    override suspend fun state(): List<SimpleView> {
        return views
    }
}