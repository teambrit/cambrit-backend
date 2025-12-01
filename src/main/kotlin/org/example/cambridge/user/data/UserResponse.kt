package org.example.cambridge.user.data

import org.example.cambridge.user.StudentAuthorizationStatus
import org.example.cambridge.user.UserRole

data class UserResponse(
    val name: String,
    val email: String,
    val role: UserRole,
    val isAuthorized: Boolean,
    val id: Long,
    val logoImage: String? = null,
    val isDeleted: Boolean,
    val studentAuthorizationStatus: StudentAuthorizationStatus,
    val university: String? = null,
    val major: String? = null,
    val phoneNumber: String? = null,
    val profileImage: String? = null,
    val description: String? = null,
    val companyUrl: String? = null,
    val companyCode: String? = null,
    val backgroundImage: String? = null,
    val bankNumber: String? = null,
    val bankName: String? = null
)
