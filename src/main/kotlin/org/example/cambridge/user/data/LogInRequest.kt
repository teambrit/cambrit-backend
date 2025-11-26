package org.example.cambridge.user.data

import org.example.cambridge.user.UserRole

data class LogInRequest(
    val email: String,
    val password: String,
    val role: UserRole
)