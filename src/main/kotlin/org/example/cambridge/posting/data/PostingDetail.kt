package org.example.cambridge.posting.data

import jakarta.validation.constraints.Email
import org.example.cambridge.posting.Posting
import org.example.cambridge.posting.PostingStatus
import org.example.cambridge.user.User
import java.time.LocalDate
import java.time.LocalDateTime

data class PostingDetail (
    val id: Long,
    val title: String,
    val body: String,
    val posterId: Long,
    val posterName: String,
    val posterEmail: String,
    val logoImage: String?,
    val compensation: Long,
    val status: PostingStatus,
    val createdAt: LocalDateTime?,
    val lastModifiedAt: LocalDateTime?,
    val applyDueDate: LocalDate?,
    val activityStartDate: LocalDate?,
    val activityEndDate: LocalDate?,
    val tags: List<String>,
    val file: String?,
    val applicantCount: Int
){
    constructor(posting: Posting, poster: User, applicantCount: Int): this(
        id = posting.id,
        title = posting.title,
        body = posting.body,
        posterId = poster.id,
        posterName = poster.name,
        posterEmail = poster.email,
        logoImage = poster.logoImage?.let {  java.util.Base64.getEncoder().encodeToString(it )},
        tags = posting.tags?.split(",")?.map { it.trim().removePrefix("[").removeSuffix("]") } ?: emptyList(),
        compensation = posting.compensation,
        status = posting.status,
        createdAt = posting.createdAt,
        lastModifiedAt = posting.lastModifiedAt,
        applyDueDate = posting.applyDueDate,
        activityStartDate = posting.activityStartDate,
        activityEndDate = posting.activityEndDate,
        file = posting.file?.let { java.util.Base64.getEncoder().encodeToString(it) },
        applicantCount = applicantCount
    )
}