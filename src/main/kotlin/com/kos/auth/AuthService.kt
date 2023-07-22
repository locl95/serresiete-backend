package com.kos.auth

import arrow.core.Either
import com.kos.auth.repository.AuthRepository

class AuthService(private val authRepository: AuthRepository) {
    suspend fun login(userName: String) = authRepository.insertToken(userName)
    fun logout(user: String) = authRepository.deleteToken(user)
    suspend fun validateCredentials(userName: String, password: String): Boolean = authRepository.validateCredentials(userName, password)
    suspend fun validateToken(token: String): Either<TokenNotFound, String> = authRepository.validateToken(token)
}