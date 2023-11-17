package com.kos.common

interface WithState<T,K> {
    suspend fun state(): T
    suspend fun withState(initialState: T): K
}