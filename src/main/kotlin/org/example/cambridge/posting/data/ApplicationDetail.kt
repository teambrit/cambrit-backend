package org.example.cambridge.posting.data

import org.example.cambridge.posting.ApplicationStatus
import java.time.LocalDateTime

data class ApplicationDetail(
    val id: Long,
    val postingId: Long,
    val postingTitle: String,
    val postingTags: List<String>,
    val posterName: String,
    val applicantId: Long,
    val applicantName: String,
    val applicantEmail: String,
    val status: ApplicationStatus,
    val createdAt: LocalDateTime,
    val verificationFile: String?,
)
