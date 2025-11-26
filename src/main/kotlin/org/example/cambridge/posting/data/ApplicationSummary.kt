package org.example.cambridge.posting.data

import org.example.cambridge.posting.ApplicationStatus
import org.example.cambridge.user.StudentAuthorizationStatus
import java.time.LocalDateTime

data class ApplicationSummary(
    val id: Long,
    val postingId: Long,
    val applicantId: Long,
    val applicantName: String,
    val applicantEmail: String,
    val status: ApplicationStatus,
    val createdAt: LocalDateTime,
    val applicantPhoneNumber: String? = null,
    val applicantProfileImage: String? = null,
    val applicantDescription: String? = null,
    val applicantUniversity: String? = null,
    val applicantMajor: String? = null,
    val applicantAuthorizationStatus: StudentAuthorizationStatus,
    val verificationFile: String?,
)
