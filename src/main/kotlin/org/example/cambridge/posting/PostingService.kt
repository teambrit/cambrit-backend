package org.example.cambridge.posting

import org.example.cambridge.posting.data.ApplicationDetail
import org.example.cambridge.posting.data.ApplicationSummary
import org.example.cambridge.posting.data.CreatePostingRequest
import org.example.cambridge.posting.data.PostingDetail
import org.example.cambridge.user.StudentAuthorizationStatus
import org.example.cambridge.user.UserRepository
import org.example.cambridge.user.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Base64

@Service
class PostingService(
    private val postingRepository: PostingRepository,
    private val userRepository: UserRepository,
    private val applicationRepository: ApplicationRepository,
    private val studentAuthorizationRequestRepository: org.example.cambridge.user.StudentAuthorizationRequestRepository,
    private val billingRepository: org.example.cambridge.billing.BillingRepository
) {
    fun createPosting(posterId: Long, postingRequest: CreatePostingRequest): PostingDetail {
        val poster = userRepository.findByIdOrNull(posterId) ?: throw IllegalArgumentException("No user with id $posterId")
        if(poster.role != UserRole.COMPANY){
            throw IllegalArgumentException("User with id $posterId is not a company")
        }
        val posting = Posting(
            title = postingRequest.title,
            body = postingRequest.body,
            posterId = posterId,
            compensation = postingRequest.compensation,
            tags = postingRequest.tags.toString(),
            status = PostingStatus.ACTIVE,
            applyDueDate = postingRequest.dueDate,
            activityStartDate = postingRequest.activityStartDate,
            activityEndDate = postingRequest.activityEndDate,
        )
        val savedPosting = postingRepository.save(posting)

        return PostingDetail(
            id = savedPosting.id,
            title = savedPosting.title,
            body = savedPosting.body,
            posterId = poster.id,
            posterName = poster.name,
            posterEmail = poster.email,
            tags = savedPosting.tags?.split(",")?.map { it.trim().removePrefix("[").removeSuffix("]") } ?: emptyList(),
            compensation = savedPosting.compensation,
            status = savedPosting.status,
            createdAt = savedPosting.createdAt,
            lastModifiedAt = savedPosting.lastModifiedAt,
            applyDueDate = savedPosting.applyDueDate,
            activityStartDate = savedPosting.activityStartDate,
            activityEndDate = savedPosting.activityEndDate,
            logoImage = poster.logoImage?.let {  Base64.getEncoder().encodeToString(it )}
        )
    }

    fun getDetail(postingId: Long): PostingDetail? {
        val posting = postingRepository.findByIdOrNull(postingId) ?: return null
        val poster = userRepository.findByIdOrNull(posting.posterId) ?: throw IllegalArgumentException("No user with id ${posting.posterId}")
        return PostingDetail(
            id = posting.id,
            title = posting.title,
            body = posting.body,
            posterId = poster.id,
            posterName = poster.name,
            posterEmail = poster.email,
            tags = posting.tags?.split(",")?.map { it.trim().removePrefix("[").removeSuffix("]") } ?: emptyList(),
            compensation = posting.compensation,
            status = posting.status,
            createdAt = posting.createdAt,
            lastModifiedAt = posting.lastModifiedAt,
            logoImage = poster.logoImage?.let {  Base64.getEncoder().encodeToString(it )},
            applyDueDate = posting.applyDueDate,
            activityStartDate = posting.activityStartDate,
            activityEndDate = posting.activityEndDate,
        )
    }

    fun getPosterPage(pageable: Pageable,posterId: Long?): Page<PostingDetail>{
        val page = if(posterId != null){
            postingRepository.findAllByPosterId(posterId,pageable)
        } else {
            postingRepository.findAllBy(pageable)
        }

        val userMap = userRepository.findAllById(page.content.map { it.posterId }).associateBy { it.id }

        return page.map { PostingDetail(it,userMap[it.posterId]!!) }
    }

    fun applyToPosting(userId: Long, postingId: Long) {
        val posting = postingRepository.findByIdOrNull(postingId) ?: throw IllegalArgumentException("No posting with id $postingId")
        val applicant = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("No user with id $userId")
        if(applicant.role != UserRole.STUDENT){
            throw IllegalArgumentException("User with id $userId is not a student")
        }
        if(posting.status != PostingStatus.ACTIVE){
            throw IllegalArgumentException("Posting with id $postingId is not active")
        }

        val existingApplication = applicationRepository.findByPostingIdAndApplicantId(postingId, userId)

        if(existingApplication != null){
            throw IllegalArgumentException("User with id $userId has already applied to posting with id $postingId")
        }

        val application = Application(
            applicantId = userId,
            postingId = postingId,
            status = ApplicationStatus.PENDING
        )

        applicationRepository.save(application)
    }

    fun getApplicationsByPosting(postingId: Long, userId: Long): List<ApplicationSummary> {
        val posting = postingRepository.findByIdOrNull(postingId) ?: throw IllegalArgumentException("No posting with id $postingId")

        if (posting.posterId != userId) {
            throw IllegalArgumentException("User with id $userId is not the poster of posting with id ${posting.id}")
        }

        val applications = applicationRepository.findAllByPostingId(postingId)
        val applicantIds = applications.map { it.applicantId }
        val applicants = userRepository.findAllById(applicantIds).associateBy { it.id }

        val authorizationMap = studentAuthorizationRequestRepository.findAll()
            .groupBy { it.userId }
            .mapValues { entry -> entry.value.maxByOrNull { it.createdAt }}

        return applications.map { application ->
            val applicant = applicants[application.applicantId] ?: throw IllegalArgumentException("No user with id ${application.applicantId}")
            val authRequest = authorizationMap[applicant.id]
            ApplicationSummary(
                id = application.id!!,
                applicantId = applicant.id,
                applicantName = applicant.name,
                applicantEmail = applicant.email,
                postingId = posting.id,
                status = application.status,
                createdAt = application.createdAt,
                applicantPhoneNumber = applicant.phoneNumber,
                applicantProfileImage = if(applicant.profileImage != null) Base64.getEncoder().encodeToString(applicant.profileImage) else null,
                applicantDescription = applicant.description,
                applicantUniversity = authRequest?.university,
                applicantMajor = authRequest?.major,
                applicantAuthorizationStatus = authRequest?.status ?: StudentAuthorizationStatus.NONE,
                verificationFile = application.verificationFile?.let { Base64.getEncoder().encodeToString(it) },
            )
        }
    }

    fun getApplicationsByUserId(userId: Long): List<ApplicationDetail>{
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("No user with id $userId")
        val applications = applicationRepository.findAllByApplicantId(userId)
        val postingIds = applications.map { it.postingId }
        val postings = postingRepository.findAllById(postingIds).associateBy { it.id }

        return applications.map { application ->
            val posting = postings[application.postingId] ?: throw IllegalArgumentException("No posting with id ${application.postingId}")
            ApplicationDetail(
                id = application.id!!,
                applicantId = user.id,
                applicantName = user.name,
                applicantEmail = user.email,
                postingId = posting.id,
                status = application.status,
                createdAt = application.createdAt,
                postingTitle = posting.title,
                postingTags = posting.tags?.split(",")?.map { it.trim().removePrefix("[").removeSuffix("]") } ?: emptyList(),
                posterName = userRepository.findByIdOrNull(posting.posterId)?.name ?: "Unknown",
                verificationFile = application.verificationFile?.let { Base64.getEncoder().encodeToString(it) },
            )
        }
    }

    @Transactional
    fun updateApplicationStatus(applicationId: Long, newStatus: ApplicationStatus, userId: Long) {
        val application = applicationRepository.findByIdOrNull(applicationId)
            ?: throw IllegalArgumentException("No application with id $applicationId")

        val posting = postingRepository.findByIdOrNull(application.postingId)
            ?: throw IllegalArgumentException("No posting with id ${application.postingId}")

        if (posting.posterId != userId) {
            throw IllegalArgumentException("User with id $userId is not the poster of posting with id ${posting.id}")
        }

        application.status = newStatus
        application.lastModifiedAt = LocalDateTime.now()
        applicationRepository.save(application)
    }

    @Transactional
    fun uploadVerificationFile(applicationId: Long, userId: Long, fileBytes: ByteArray) {
        val application = applicationRepository.findByIdOrNull(applicationId)
            ?: throw IllegalArgumentException("No application with id $applicationId")

        if (application.applicantId != userId) {
            throw IllegalArgumentException("User with id $userId is not the applicant of this application")
        }

        if (application.status != ApplicationStatus.APPROVED) {
            throw IllegalArgumentException("Only approved applications can upload verification files")
        }

        application.verificationFile = fileBytes
        application.lastModifiedAt = LocalDateTime.now()
        applicationRepository.save(application)

        if(application.billingId != null){
            return // 이미 빌링처리되었다면 리턴
        }

        // Billing logic
        val posting = postingRepository.findByIdOrNull(application.postingId)
            ?: throw IllegalArgumentException("No posting with id ${application.postingId}")

        val companyId = posting.posterId

        // Calculate this month's start and end dates
        val now = LocalDate.now()
        val monthStart = now.withDayOfMonth(1).atStartOfDay()
        val monthEnd = now.withDayOfMonth(1).atStartOfDay().plusMonths(1).minusSeconds(1)

        // Find or create billing for this month
        var billing = billingRepository.findByCompanyIdAndStartedAtAndEndedAt(companyId, monthStart, monthEnd)

        if (billing == null) {
            // Create new billing for this month
            billing = org.example.cambridge.billing.Billing(
                companyId = companyId,
                startedAt = monthStart,
                endedAt = monthEnd,
                totalAmount = 10000L,
                status = org.example.cambridge.billing.BillingStatus.PENDING,
            )
            billingRepository.save(billing)
        } else {
            // Add 10000 to existing billing
            billing.totalAmount += 10000L
            billingRepository.save(billing)
        }
        application.billingId = billing.id
        application.chargedDate = LocalDateTime.now()
    }
}