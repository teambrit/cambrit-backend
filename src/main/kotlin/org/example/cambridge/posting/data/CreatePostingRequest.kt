package org.example.cambridge.posting.data

import java.time.LocalDate

data class CreatePostingRequest (
    val title: String,
    val body: String,
    val compensation: Long,
    val tags: List<String>,
    val applyDueDate: LocalDate?,
    val activityStartDate: LocalDate?,
    val activityEndDate: LocalDate?,
)