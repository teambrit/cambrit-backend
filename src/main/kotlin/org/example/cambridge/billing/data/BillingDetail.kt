package org.example.cambridge.billing.data

import org.example.cambridge.billing.BillingStatus
import java.time.LocalDateTime

data class BillingDetailItem(
    val postingId: Long,
    val postingTitle: String,
    val studentId: Long,
    val studentName: String,
    val chargedDate: LocalDateTime
)

data class BillingDetail(
    val id: Long,
    val companyId: Long,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val totalAmount: Long,
    val status: BillingStatus,
    val items: List<BillingDetailItem>
)