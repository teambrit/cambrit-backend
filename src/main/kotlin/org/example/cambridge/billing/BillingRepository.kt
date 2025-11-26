package org.example.cambridge.billing

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRepository : JpaRepository<Billing, Long> {
    fun findAllByCompanyId(companyId: Long): List<Billing>
    fun findAllByStatus(status: BillingStatus): List<Billing>
    fun findAllByCompanyIdAndStatus(companyId: Long, status: BillingStatus): List<Billing>
    fun findByCompanyIdAndStartedAtAndEndedAt(companyId: Long, startedAt: LocalDateTime, endedAt: LocalDateTime): Billing?
}
