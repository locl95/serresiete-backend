package com.kos.common

interface WithState<T> {
    suspend fun state(): T
}