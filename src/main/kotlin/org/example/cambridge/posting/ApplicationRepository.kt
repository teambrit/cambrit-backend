package org.example.cambridge.posting

import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationRepository: JpaRepository<Application,Long> {
    fun findAllByPostingId(postingId: Long): List<Application>

    fun findAllByPostingIdIn(postingIds: List<Long>): List<Application>

    fun findAllByApplicantId(applicantId: Long): List<Application>

    fun findByPostingIdAndApplicantId(postingId: Long, applicantId: Long): Application?

    fun findAllByBillingId(billingId: Long): List<Application>
}