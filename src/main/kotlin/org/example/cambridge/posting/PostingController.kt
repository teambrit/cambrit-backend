package org.example.cambridge.posting

import io.swagger.v3.oas.annotations.Operation
import org.example.cambridge.configuration.JwtUtil
import org.example.cambridge.posting.data.ApplicationDetail
import org.example.cambridge.posting.data.ApplicationSummary
import org.example.cambridge.posting.data.CreatePostingRequest
import org.example.cambridge.posting.data.PostingDetail
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/posting")
@CrossOrigin(origins = ["http://localhost:3000","http://localhost:5173","http://campus-bridge.s3-website.ap-northeast-2.amazonaws.com"], allowCredentials = "true")
class PostingController(
    private val postingService: PostingService,
    private val jwtUtils: JwtUtil
) {
    @PostMapping("")
    @Operation(summary = "공고 생성", description = "새로운 공고를 생성합니다.")
    fun createPosting(
        @RequestBody request: CreatePostingRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<PostingDetail>{
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        val res = postingService.createPosting(userId,request)
        return ResponseEntity.status(201).body(res)
    }

    @PutMapping("/{id}")
    @Operation(summary = "공고 수정", description = "자신이 작성한 공고를 수정합니다.")
    fun updatePosting(
        @PathVariable id: Long,
        @RequestBody request: CreatePostingRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<PostingDetail> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        val res = postingService.updatePosting(id, userId, request)
        return ResponseEntity.ok(res)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공고 삭제", description = "자신이 작성한 공고를 삭제합니다. (지원자가 없는 경우만 가능)")
    fun deletePosting(
        @PathVariable id: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        postingService.deletePosting(id, userId)
        return ResponseEntity.ok("Posting deleted successfully")
    }

    @GetMapping("/{id}")
    @Operation(summary = "공고 상세 조회", description = "특정 공고의 상세 정보를 조회합니다.")
    fun getPostingDetail(
        @PathVariable id: Long
    ): ResponseEntity<PostingDetail> {
        val res = postingService.getDetail(id) ?: return ResponseEntity.status(404).build()
        return ResponseEntity.ok(res)
    }

    @GetMapping("/page")
    @Operation(summary = "공고 페이징 조회", description = "공고를 페이징하여 조회합니다.")
    fun getPostingPage(
        @ParameterObject
        pageable: Pageable,
        @RequestParam(required = false) posterId: Long? = null
    ): ResponseEntity<Page<PostingDetail>> {
        val res = postingService.getPosterPage(pageable,posterId)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/apply/{postingId}")
    @Operation(summary = "공고 지원", description = "특정 공고에 지원합니다.")
    fun applyToPosting(
        @PathVariable postingId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        postingService.applyToPosting(userId,postingId)
        return ResponseEntity.ok("Applied to posting $postingId")
    }

    @GetMapping("/{postingId}/applications")
    @Operation(summary = "공고 지원자 조회", description = "특정 공고에 지원한 지원자 목록을 조회합니다.")
    fun getApplicationsForPosting(
        @PathVariable postingId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ApplicationSummary>> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        val res = postingService.getApplicationsByPosting(postingId, userId)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/applications/me")
    @Operation(summary = "내 지원 목록 조회", description = "내가 지원한 공고 목록을 조회합니다.")
    fun getMyApplications(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ApplicationDetail>> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        val res = postingService.getApplicationsByUserId(userId)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/{postingId}/application-status")
    @Operation(summary = "특정 공고 지원 여부 조회", description = "현재 사용자가 특정 공고에 지원했는지 확인합니다. 지원하지 않았으면 204 No Content를 반환합니다.")
    fun checkApplicationStatus(
        @PathVariable postingId: Long,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApplicationDetail> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        val res = postingService.checkApplicationStatus(postingId, userId)
            ?: return ResponseEntity.status(204).build()
        return ResponseEntity.ok(res)
    }

    @PutMapping("/applications/{applicationId}/status")
    @Operation(summary = "지원 상태 변경 (포스팅 작성자용)", description = "포스팅 작성자가 지원자의 상태를 변경합니다.")
    fun updateApplicationStatus(
        @PathVariable applicationId: Long,
        @RequestParam("status") status: ApplicationStatus,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        postingService.updateApplicationStatus(applicationId, status, userId)
        return ResponseEntity.ok("Application status updated")
    }

    @PutMapping("/applications/{applicationId}/verification-file", consumes = ["multipart/form-data"])
    @Operation(summary = "인증 파일 업로드 (승인된 지원자용)", description = "승인된 지원자가 인증 파일을 업로드합니다.")
    fun uploadVerificationFile(
        @PathVariable applicationId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        val userId = jwtUtils.validateAndGetSubject(token.removePrefix("Bearer "))?.toLong()
            ?: return ResponseEntity.status(401).build()
        postingService.uploadVerificationFile(applicationId, userId, file.bytes)
        return ResponseEntity.ok("Verification file uploaded successfully")
    }


}