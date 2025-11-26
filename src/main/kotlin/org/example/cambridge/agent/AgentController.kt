package org.example.cambridge.agent

import io.swagger.v3.oas.annotations.Operation
import org.example.cambridge.configuration.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = ["http://localhost:3000","http://localhost:5173","http://campus-bridge.s3-website.ap-northeast-2.amazonaws.com"], allowCredentials = "true")
class AgentController(
    private val agentService: AgentService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/chat")
    @Operation(
        summary = "AI Agent와 채팅",
        description = "사용자의 메시지를 AI Agent에게 전달하고 응답을 받습니다. sessionId를 지정하면 기존 대화를 이어가고, 없으면 새 세션을 생성합니다."
    )
    fun chat(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: ChatRequest
    ): ResponseEntity<ChatResponse> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer "))
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val response = agentService.chat(
            userId = userId.toLong(),
            userMessage = request.message,
            sessionId = request.sessionId
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/sessions")
    @Operation(
        summary = "채팅 세션 목록 조회",
        description = "현재 사용자의 모든 채팅 세션 목록을 조회합니다."
    )
    fun getSessions(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ChatSessionSummary>> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer "))
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val sessions = agentService.getSessions(userId.toLong())
        return ResponseEntity.ok(sessions)
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(
        summary = "채팅 세션 메시지 조회",
        description = "특정 세션의 모든 메시지를 조회합니다."
    )
    fun getSessionMessages(
        @RequestHeader("Authorization") token: String,
        @PathVariable sessionId: Long
    ): ResponseEntity<List<ChatMessageResponse>> {
        val userId = jwtUtil.validateAndGetSubject(token.removePrefix("Bearer "))
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val messages = agentService.getSessionMessages(userId.toLong(), sessionId)
            ResponseEntity.ok(messages)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}

// Request DTO
data class ChatRequest(
    val message: String,
    val sessionId: Long? = null
)