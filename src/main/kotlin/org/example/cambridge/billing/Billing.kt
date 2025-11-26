package org.example.cambridge.billing

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "billing")
class Billing(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "company_id", nullable = true)
    var companyId: Long? = null,

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ended_at", nullable = false)
    var endedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "total_amount", nullable = false)
    var totalAmount: Long = 0L,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: BillingStatus = BillingStatus.PENDING,

)