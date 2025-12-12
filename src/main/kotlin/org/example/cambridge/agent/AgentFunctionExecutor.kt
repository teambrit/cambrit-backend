package org.example.cambridge.agent

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.cambridge.billing.BillingService
import org.example.cambridge.posting.PostingService
import org.example.cambridge.user.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class AgentFunctionExecutor(
    private val userService: UserService,
    private val postingService: PostingService,
    private val billingService: BillingService,
    private val objectMapper: ObjectMapper
) {
    // 이미지 필드를 제거하는 헬퍼 함수
    private fun removeImageFields(obj: Any): Any {
        val jsonNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(obj)
        removeImageFieldsRecursive(jsonNode)
        return objectMapper.writeValueAsString(jsonNode)
    }

    private fun removeImageFieldsRecursive(node: com.fasterxml.jackson.databind.JsonNode) {
        if (node.isObject) {
            val objectNode = node as com.fasterxml.jackson.databind.node.ObjectNode
            // 이미지 및 파일 관련 필드 제거 (Base64 데이터가 매우 클 수 있음)
            objectNode.remove("profileImage")
            objectNode.remove("logoImage")
            objectNode.remove("backgroundImage")
            objectNode.remove("verificationFile")
            objectNode.remove("applicantProfileImage")
            // 추가로 다른 이름으로 된 이미지 필드도 제거
            objectNode.remove("image")
            objectNode.remove("logo")
            objectNode.remove("thumbnail")
            objectNode.remove("photo")
            objectNode.remove("file")
            objectNode.remove("attachment")

            // 자식 노드들도 재귀적으로 처리
            objectNode.fields().forEach { (_, value) ->
                removeImageFieldsRecursive(value)
            }
        } else if (node.isArray) {
            node.forEach { removeImageFieldsRecursive(it) }
        }
    }

    fun executeFunction(functionName: String, argumentsJson: String, currentUserId: Long): String {
        return try {
            when (functionName) {
                "get_user_info" -> {
                    val args = objectMapper.readValue(argumentsJson, GetUserInfoArgs::class.java)
                    val userId = args.userId ?: currentUserId
                    val user = userService.getUserById(userId)
                    removeImageFields(user) as String
                }
                "get_posting_list" -> {
                    val args = objectMapper.readValue(argumentsJson, GetPostingListArgs::class.java)
                    val page = args.page ?: 0
                    val size = args.size ?: 20
                    // 기업 회원은 자동으로 자신의 공고만 조회
                    val currentUser = userService.getUserById(currentUserId)
                    val posterId = if (currentUser.role == org.example.cambridge.user.UserRole.COMPANY) {
                        currentUserId
                    } else {
                        args.posterId
                    }
                    val postings = postingService.getPosterPage(PageRequest.of(page, size), posterId)
                    removeImageFields(postings) as String
                }
                "get_posting_detail" -> {
                    val args = objectMapper.readValue(argumentsJson, GetPostingDetailArgs::class.java)
                    val posting = postingService.getDetail(args.postingId)
                    if (posting == null) {
                        objectMapper.writeValueAsString(mapOf("error" to "Posting not found"))
                    } else {
                        removeImageFields(posting) as String
                    }
                }
                "filter_postings" -> {
                    val args = objectMapper.readValue(argumentsJson, FilterPostingsArgs::class.java)
                    val postings = args.postingIds.mapNotNull { id ->
                        postingService.getDetail(id)
                    }
                    removeImageFields(postings) as String
                }
                "get_my_applications" -> {
                    val applications = postingService.getApplicationsByUserId(currentUserId)
                    removeImageFields(applications) as String
                }
                "get_my_postings" -> {
                    val args = objectMapper.readValue(argumentsJson, GetPostingListArgs::class.java)
                    val page = args.page ?: 0
                    val size = args.size ?: 20
                    val postings = postingService.getPosterPage(PageRequest.of(page, size), currentUserId)
                    removeImageFields(postings) as String
                }
                "get_applications_for_posting" -> {
                    val args = objectMapper.readValue(argumentsJson, GetApplicationsArgs::class.java)
                    val applications = postingService.getApplicationsByPosting(args.postingId, currentUserId)
                    removeImageFields(applications) as String
                }
                "get_billing_list" -> {
                    val billings = billingService.getAllBillingsByCompany(currentUserId)
                    removeImageFields(billings) as String
                }
                "get_billing_detail" -> {
                    val args = objectMapper.readValue(argumentsJson, GetBillingDetailArgs::class.java)
                    val billing = billingService.getBillingDetail(args.billingId, currentUserId)
                    removeImageFields(billing) as String
                }
                "update_user_profile" -> {
                    val args = objectMapper.readValue(argumentsJson, UpdateUserProfileArgs::class.java)
                    userService.updateUserProfile(
                        userId = currentUserId,
                        name = args.name,
                        phoneNumber = args.phoneNumber,
                        description = args.description,
                        profileImage = null, // 이미지는 agent에서 수정 불가
                        bankNumber = args.bankNumber,
                        bankName = args.bankName
                    )
                    objectMapper.writeValueAsString(mapOf("success" to true, "message" to "프로필이 업데이트되었습니다."))
                }
                "update_company_profile" -> {
                    val args = objectMapper.readValue(argumentsJson, UpdateCompanyProfileArgs::class.java)
                    userService.updateCompanyProfile(
                        userId = currentUserId,
                        name = args.name,
                        companyCode = args.companyCode,
                        companyUrl = args.companyUrl,
                        description = args.description,
                        logoImage = null,
                        backgroundImage = null,
                        bankNumber = args.bankNumber,
                        bankName = args.bankName
                    )
                    objectMapper.writeValueAsString(mapOf("success" to true, "message" to "회사 프로필이 업데이트되었습니다."))
                }
                "create_posting" -> {
                    val args = objectMapper.readValue(argumentsJson, CreatePostingArgs::class.java)
                    val request = org.example.cambridge.posting.data.CreatePostingRequest(
                        title = args.title,
                        body = args.body,
                        compensation = args.compensation,
                        tags = args.tags?.split(",")?.map { it.trim() } ?: emptyList(),
                        applyDueDate = null,
                        activityStartDate = null,
                        activityEndDate = null
                    )
                    val posting = postingService.createPosting(currentUserId, request)
                    removeImageFields(posting) as String
                }
                "update_posting" -> {
                    val args = objectMapper.readValue(argumentsJson, UpdatePostingArgs::class.java)
                    val request = org.example.cambridge.posting.data.CreatePostingRequest(
                        title = args.title,
                        body = args.body,
                        compensation = args.compensation,
                        tags = args.tags?.split(",")?.map { it.trim() } ?: emptyList(),
                        applyDueDate = null,
                        activityStartDate = null,
                        activityEndDate = null
                    )
                    val posting = postingService.updatePosting(args.postingId, currentUserId, request)
                    removeImageFields(posting) as String
                }
                "delete_posting" -> {
                    val args = objectMapper.readValue(argumentsJson, DeletePostingArgs::class.java)
                    postingService.deletePosting(args.postingId, currentUserId)
                    objectMapper.writeValueAsString(mapOf("success" to true, "message" to "공고가 삭제되었습니다."))
                }
                "apply_to_posting" -> {
                    val args = objectMapper.readValue(argumentsJson, ApplyToPostingArgs::class.java)
                    postingService.applyToPosting(currentUserId, args.postingId)
                    objectMapper.writeValueAsString(mapOf("success" to true, "message" to "공고에 지원했습니다."))
                }
                "update_application_status" -> {
                    val args = objectMapper.readValue(argumentsJson, UpdateApplicationStatusArgs::class.java)
                    val status = org.example.cambridge.posting.ApplicationStatus.valueOf(args.status)
                    postingService.updateApplicationStatus(args.applicationId, status, currentUserId)
                    objectMapper.writeValueAsString(mapOf("success" to true, "message" to "지원 상태가 변경되었습니다."))
                }
                else -> {
                    objectMapper.writeValueAsString(mapOf("error" to "Unknown function: $functionName"))
                }
            }
        } catch (e: Exception) {
            objectMapper.writeValueAsString(mapOf("error" to e.message))
        }
    }
}

// Argument classes
data class GetUserInfoArgs(val userId: Long?)
data class GetPostingListArgs(val page: Int?, val size: Int?, val posterId: Long?)
data class GetPostingDetailArgs(val postingId: Long)
data class FilterPostingsArgs(val postingIds: List<Long>)
data class GetApplicationsArgs(val postingId: Long)
data class GetBillingDetailArgs(val billingId: Long)
data class UpdateUserProfileArgs(val name: String, val phoneNumber: String?, val description: String?, val bankNumber: String?, val bankName: String?)
data class UpdateCompanyProfileArgs(val name: String, val companyCode: String?, val companyUrl: String?, val description: String?, val bankNumber: String?, val bankName: String?)
data class CreatePostingArgs(val title: String, val body: String, val compensation: Long, val tags: String?)
data class UpdatePostingArgs(val postingId: Long, val title: String, val body: String, val compensation: Long, val tags: String?)
data class DeletePostingArgs(val postingId: Long)
data class ApplyToPostingArgs(val postingId: Long)
data class UpdateApplicationStatusArgs(val applicationId: Long, val status: String)