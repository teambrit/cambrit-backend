package org.example.cambridge.billing.data

import org.example.cambridge.billing.BillingStatus
import java.time.LocalDateTime

data class BillingResponse(
    val id: Long,
    val companyId: Long,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val totalAmount: Long,
    val status: BillingStatus
)