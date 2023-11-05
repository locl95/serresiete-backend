package com.kos.auth

import arrow.core.Either
import com.kos.auth.repository.AuthRepository

class AuthService(private val authRepository: AuthRepository) {
    suspend fun login(userName: String) = authRepository.insertToken(userName)
    suspend fun logout(user: String) = authRepository.deleteToken(user)
    suspend fun validateToken(token: String): Either<TokenError, String> = authRepository.validateToken(token)
}