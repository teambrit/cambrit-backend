package org.example.cambridge.billing

import io.swagger.v3.oas.annotations.Operation
import org.example.cambridge.billing.data.BillingDetail
import org.example.cambridge.billing.data.BillingResponse
import org.example.cambridge.configuration.JwtUtil
import org.example.cambridge.user.UserRepository
import org.example.cambridge.user.UserRole
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/billing")
@CrossOrigin(origins = ["http://localhost:3000","http://localhost:5173","http://campus-bridge.s3-website.ap-northeast-2.amazonaws.com"], allowCredentials = "true")
class BillingController(
    private val billingService: BillingService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) {

    @GetMapping("/my")
    @Operation(summary = "내 빌링 리스트 조회 (회사용)", description = "회사가 자신의 모든 빌링 내역을 조회합니다.")
    fun getMyBillings(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<BillingResponse>> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val user = userRepository.findByIdOrNull(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (user.role != UserRole.COMPANY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val billings = billingService.getAllBillingsByCompany(userId)
        return ResponseEntity.ok(billings)
    }

    @GetMapping("/{billingId}")
    @Operation(summary = "빌링 상세 조회 (회사용)", description = "특정 빌링의 상세 정보를 조회합니다. 공고, 학생 정보가 포함됩니다.")
    fun getBillingDetail(
        @PathVariable billingId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<BillingDetail> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val user = userRepository.findByIdOrNull(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (user.role != UserRole.COMPANY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val billingDetail = billingService.getBillingDetail(billingId, userId)
        return ResponseEntity.ok(billingDetail)
    }
}