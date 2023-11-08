package com.kos.common

interface WithState<T> {
    suspend fun state(): T
    suspend fun withState(initialState: T): Repository
}