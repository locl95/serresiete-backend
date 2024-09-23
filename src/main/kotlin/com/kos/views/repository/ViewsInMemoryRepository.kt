package com.kos.views.repository

import com.kos.common.InMemoryRepository
import com.kos.views.SimpleView
import com.kos.views.ViewDeleted
import com.kos.views.ViewModified
import java.util.*

class ViewsInMemoryRepository : ViewsRepository, InMemoryRepository {

    private val views: MutableList<SimpleView> = mutableListOf()

    override suspend fun getOwnViews(owner: String): List<SimpleView> = views.filter { it.owner == owner }

    override suspend fun get(id: String): SimpleView? = views.find { it.id == id }

    override suspend fun create(name: String, owner: String, characterIds: List<Long>): ViewModified {
        val id = UUID.randomUUID().toString()
        views.add(SimpleView(id, name, owner, true, characterIds)) // All views are visible by default
        return ViewModified(id, characterIds)
    }

    override suspend fun edit(id: String, name: String, published: Boolean, characters: List<Long>): ViewModified {
        val index = views.indexOfFirst { it.id == id }
        val oldView = views[index]
        views.removeAt(index)
        views.add(index, SimpleView(id, name, oldView.owner, published, characters))
        return ViewModified(id, characters)
    }

    override suspend fun patch(id: String, name: String?, published: Boolean?, characters: List<Long>?): ViewModified {
        val index = views.indexOfFirst { it.id == id }
        val oldView = views[index]
        views.removeAt(index)
        val simpleView = SimpleView(
            id,
            name ?: oldView.name,
            oldView.owner,
            published ?: oldView.published,
            characters ?: oldView.characterIds
        )
        views.add(
            index,
            simpleView
        )
        return ViewModified(id, simpleView.characterIds)
    }

    override suspend fun delete(id: String): ViewDeleted {
        val index = views.indexOfFirst { it.id == id }
        views.removeAt(index)
        return ViewDeleted(id)
    }

    override suspend fun getViews(): List<SimpleView> {
        return views
    }

    override suspend fun state(): List<SimpleView> {
        return views
    }

    override suspend fun withState(initialState: List<SimpleView>): ViewsInMemoryRepository {
        views.addAll(initialState)
        return this
    }

    override fun clear() {
        views.clear()
    }
}