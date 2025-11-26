package org.example.cambridge.user

import io.swagger.v3.oas.annotations.Operation
import org.example.cambridge.configuration.JwtUtil
import org.example.cambridge.user.data.LogInRequest
import org.example.cambridge.user.data.SignUpRequest
import org.example.cambridge.user.data.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = ["http://localhost:3000","http://localhost:5173","http://campus-bridge.s3-website.ap-northeast-2.amazonaws.com"], allowCredentials = "true")
class UserController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입")
    fun signup(@RequestBody request: SignUpRequest): ResponseEntity<String> {
        val res = userService.createUser(
            name = request.name,
            email = request.email,
            password = request.password,
            role = request.role
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(res)
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "성공시 access token 발급")
    fun logIn(@RequestBody request: LogInRequest): ResponseEntity<String> {
        val token = userService.login(
            email = request.email,
            rawPassword = request.password,
            role = request.role
        ) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
        return ResponseEntity.status(HttpStatus.OK).body(token)
    }

    @GetMapping("/token-check")
    @Operation(summary = "토큰 체크", description = "유효한 토큰인지 확인")
    fun tokenCheck(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body("Token is valid")
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "토큰에서 사용자 정보 추출")
    fun getMyInfo(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<UserResponse> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val userResponse = userService.getUserById(userId.toLong())
        return ResponseEntity.status(HttpStatus.OK).body(userResponse)
    }

    @PostMapping("/student-authorization-request", consumes = ["multipart/form-data"])
    @Operation(summary = "학생 인증 요청", description = "학생 인증을 요청합니다. 파일(재학증명서 등)을 업로드해야 합니다.")
    fun postStudentAuthorization(
        @RequestHeader("Authorization") token: String,
        @RequestParam("university") university: String,
        @RequestParam("major") major: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.requestStudentAuthorization(userId.toLong(), university, major, file.bytes)
        return ResponseEntity.status(HttpStatus.OK).body("Student authorization request submitted")
    }

    @GetMapping("/student-authorization-request/status")
    @Operation(summary = "학생 인증 요청 상태 조회", description = "내 학생 인증 요청 상태를 조회합니다.")
    fun getAllStudentAuthorizationRequests(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<StudentAuthorizationStatus> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val status = userService.getStudentAuthorizationStatus(userId.toLong())
        return ResponseEntity.status(HttpStatus.OK).body(status)
    }

    @GetMapping("/student/all")
    @Operation(summary = "모든 학생 회원 조회 (관리자용)", description = "모든 학생 회원의 정보를 조회합니다. 관리자만 접근할 수 있습니다.")
    fun getAllStudents(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<UserResponse>> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val user = userService.getUserById(userId.toLong())
        if (user.role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val students = userService.getAllStudents()
        return ResponseEntity.status(HttpStatus.OK).body(students)
    }
    @GetMapping("/student-authorization-request/{userId}/file")
    @Operation(summary = "학생 인증 요청 파일 조회 (관리자용)", description = "특정 학생 회원의 가장 최근 인증 요청 파일(재학증명서 등)을 조회합니다. 관리자만 접근할 수 있습니다.")
    fun getStudentAuthorizationRequestFile(
        @RequestHeader("Authorization") token: String,
        @PathVariable userId: Long
    ): ResponseEntity<ByteArray> {
        val adminId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val adminUser = userService.getUserById(adminId.toLong())
        if (adminUser.role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val fileBytes = userService.getStudentAuthorizationFile(userId)
        val extension: String = detectImageFormat(fileBytes)
        val contentType: String = getContentType(extension)

        return ResponseEntity.ok()
            .header("Content-Type", contentType)
            .header("Content-Disposition", "attachment; filename=\"authorization_request_$userId\"")
            .body(fileBytes)
    }

    @PutMapping("/student-authorization-request/{userId}")
    @Operation(summary = "학생 인증 요청 승인/거절 (관리자용)", description = "특정 학생 회원의 인증 요청을 승인하거나 거절합니다. 관리자만 접근할 수 있습니다.")
    fun reviewStudentAuthorizationRequest(
        @RequestHeader("Authorization") token: String,
        @PathVariable userId: Long,
        @RequestParam("approve") approve: Boolean
    ): ResponseEntity<String> {
        val adminId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val adminUser = userService.getUserById(adminId.toLong())
        if (adminUser.role != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        userService.reviewStudentAuthorizationRequest(userId, approve)
        return ResponseEntity.status(HttpStatus.OK).body("Student authorization request reviewed")
    }

    @PutMapping("/profile", consumes = ["multipart/form-data"])
    @Operation(summary = "프로필 업데이트", description = "사용자(학생)가 자신의 프로필 정보를 업데이트합니다.")
    fun updateProfile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("name") name: String,
        @RequestParam("phoneNumber", required = false) phoneNumber: String?,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("profileImage", required = false) profileImage: MultipartFile?
    ): ResponseEntity<String> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.updateUserProfile(
            userId = userId.toLong(),
            name = name,
            phoneNumber = phoneNumber,
            description = description,
            profileImage = profileImage?.bytes
        )
        return ResponseEntity.status(HttpStatus.OK).body("Profile updated successfully")
    }


    @PutMapping("/company/profile", consumes = ["multipart/form-data"])
    @Operation(summary = "회사 프로필 수정", description = "회사가 자신의 프로필 정보를 수정합니다.")
    fun updateCompanyProfile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("name") name: String,
        @RequestParam("companyCode", required = false) companyCode: String?,
        @RequestParam("companyUrl", required = false) companyUrl: String?,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("logoImage", required = false) logoImage: MultipartFile?,
        @RequestParam("backgroundImage", required = false) backgroundImage: MultipartFile?
    ): ResponseEntity<String> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer ")) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.updateCompanyProfile(
            userId = userId.toLong(),
            name = name,
            companyCode = companyCode,
            companyUrl = companyUrl,
            description = description,
            logoImage = logoImage?.bytes,
            backgroundImage = backgroundImage?.bytes
        )
        return ResponseEntity.status(HttpStatus.OK).body("Company profile updated successfully")
    }

    private fun detectImageFormat(data: ByteArray): String {
        if (data.size < 12) return "bin"


        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (data[0] == 0x89.toByte() && data[1].toInt() == 0x50 && data[2].toInt() == 0x4E && data[3].toInt() == 0x47) {
            return "png"
        }


        // JPEG: FF D8 FF
        if (data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() && data[2] == 0xFF.toByte()) {
            return "jpg"
        }


        // GIF: 47 49 46 38
        if (data[0].toInt() == 0x47 && data[1].toInt() == 0x49 && data[2].toInt() == 0x46 && data[3].toInt() == 0x38) {
            return "gif"
        }


        // WebP: RIFF....WEBP
        if (data[0].toInt() == 0x52 && data[1].toInt() == 0x49 && data[2].toInt() == 0x46 && data[3].toInt() == 0x46 && data[8].toInt() == 0x57 && data[9].toInt() == 0x45 && data[10].toInt() == 0x42 && data[11].toInt() == 0x50) {
            return "webp"
        }

        // PDF: 25 50 44 46 (%PDF)
        if (data[0].toInt() == 0x25 && data[1].toInt() == 0x50 && data[2].toInt() == 0x44 && data[3].toInt() == 0x46) {
            return "pdf"
        }

        return "bin"
    }

    private fun getContentType(extension: String): String {
        when (extension.lowercase(Locale.getDefault())) {
            "png" -> return "image/png"
            "jpg", "jpeg" -> return "image/jpeg"
            "gif" -> return "image/gif"
            "webp" -> return "image/webp"
            "pdf" -> return "application/pdf"
            else -> return "application/octet-stream"
        }
    }

}