package org.example.cambridge.user.data

import org.example.cambridge.user.UserRole

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole
)
