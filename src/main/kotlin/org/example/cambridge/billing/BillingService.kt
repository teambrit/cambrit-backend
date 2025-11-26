package org.example.cambridge.billing

import org.example.cambridge.billing.data.BillingDetail
import org.example.cambridge.billing.data.BillingDetailItem
import org.example.cambridge.billing.data.BillingResponse
import org.example.cambridge.posting.ApplicationRepository
import org.example.cambridge.posting.PostingRepository
import org.example.cambridge.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BillingService(
    private val billingRepository: BillingRepository,
    private val applicationRepository: ApplicationRepository,
    private val postingRepository: PostingRepository,
    private val userRepository: UserRepository
) {
    fun getAllBillingsByCompany(companyId: Long): List<BillingResponse> {
        val billings = billingRepository.findAllByCompanyId(companyId)
        return billings.map { billing ->
            BillingResponse(
                id = billing.id!!,
                companyId = billing.companyId!!,
                startedAt = billing.startedAt,
                endedAt = billing.endedAt,
                totalAmount = billing.totalAmount,
                status = billing.status
            )
        }
    }

    fun getBillingDetail(billingId: Long, companyId: Long): BillingDetail {
        val billing = billingRepository.findByIdOrNull(billingId)
            ?: throw IllegalArgumentException("No billing with id $billingId")

        if (billing.companyId != companyId) {
            throw IllegalArgumentException("User with id $companyId is not the owner of billing with id $billingId")
        }

        val applications = applicationRepository.findAllByBillingId(billingId)
        val postingIds = applications.map { it.postingId }.distinct()
        val postings = postingRepository.findAllById(postingIds).associateBy { it.id }

        val applicantIds = applications.map { it.applicantId }.distinct()
        val applicants = userRepository.findAllById(applicantIds).associateBy { it.id }

        val items = applications.mapNotNull { application ->
            val posting = postings[application.postingId]
            val applicant = applicants[application.applicantId]

            if (posting != null && applicant != null && application.chargedDate != null) {
                BillingDetailItem(
                    postingId = posting.id,
                    postingTitle = posting.title,
                    studentId = applicant.id,
                    studentName = applicant.name,
                    chargedDate = application.chargedDate!!
                )
            } else {
                null
            }
        }

        return BillingDetail(
            id = billing.id!!,
            companyId = billing.companyId!!,
            startedAt = billing.startedAt,
            endedAt = billing.endedAt,
            totalAmount = billing.totalAmount,
            status = billing.status,
            items = items
        )
    }
}