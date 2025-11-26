package org.example.cambridge.agent

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.cambridge.openai.OpenAiService
import org.example.cambridge.openai.dto.OpenAiChatRequest
import org.example.cambridge.openai.dto.OpenAiMessage
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AgentService(
    private val openAiService: OpenAiService,
    private val agentFunctionExecutor: AgentFunctionExecutor,
    private val chatSessionRepository: ChatSessionRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun chat(userId: Long, userMessage: String, sessionId: Long?): ChatResponse {
        // 1. 세션 생성 또는 조회
        val session = if (sessionId != null) {
            chatSessionRepository.findByIdAndUserId(sessionId, userId)
                ?: throw IllegalArgumentException("Session not found or unauthorized")
        } else {
            // 새 세션 생성
            val newSession = ChatSession(
                userId = userId,
                title = userMessage.take(100) // 첫 메시지를 제목으로
            )
            chatSessionRepository.save(newSession)
        }

        // 2. 사용자 메시지 저장
        val userChatMessage = ChatMessage(
            sessionId = session.id,
            role = "user",
            content = userMessage
        )
        chatMessageRepository.save(userChatMessage)

        // 3. 기존 대화 내역 로드 (user, assistant만, 최근 10쌍만)
        val previousMessages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.id)
        // tool 메시지는 제외 (OpenAI API 순서 요구사항 때문)
        val userAssistantMessages = previousMessages.filter { it.role == "user" || it.role == "assistant" }
        val recentMessages = if (userAssistantMessages.size > 20) {
            userAssistantMessages.takeLast(20)
        } else {
            userAssistantMessages
        }
        val messages = mutableListOf<OpenAiMessage>()

        // 시스템 프롬프트 추가
        messages.add(OpenAiMessage(
            role = "system",
            content = """당신은 캠퍼스 브릿지 서비스의 AI 어시스턴트입니다.
캠퍼스 브릿지는 기업과 학생을 연결하는 채용/인턴십 플랫폼입니다.

[지원하는 기능]
- 사용자 정보 조회 및 프로필 수정
- 공고 조회, 생성, 검색
- 공고 지원 및 지원 내역 확인
- 지원자 관리 (승인/거절)
- 청구서 조회

[중요 규칙]
1. 위 기능과 관련된 요청만 처리하세요.
2. 서비스와 관련 없는 질문(공부법, 일반 상식, 코딩, 날씨 등)은 정중히 거절하세요.
3. 데이터 조회 후에는 "조회했습니다", "완료했습니다" 등 간단한 확인 메시지만 전달하세요.
4. 조회된 데이터의 상세 내용(목록, 숫자, 이름 등)을 메시지에 나열하지 마세요. 데이터는 별도로 전달됩니다.
5. 오류가 발생하면 사용자에게 이해하기 쉽게 설명해주세요.
6. 조회 요청이 오면 반드시 해당 함수를 호출하세요. 이전에 비슷한 작업을 했더라도 매번 새로 조회해야 합니다.
7. 절대로 함수 호출 없이 "조회했습니다"라고 응답하지 마세요.
[거절 예시]
- "시험 잘 보는 법 알려줘" → "죄송합니다. 저는 캠퍼스 브릿지 서비스 관련 기능만 도와드릴 수 있어요. 공고 조회, 지원, 프로필 관리 등을 요청해 주세요!"
- "오늘 날씨 어때?" → "죄송합니다. 저는 캠퍼스 브릿지 서비스 전용 어시스턴트예요. 공고나 지원 관련 도움이 필요하시면 말씀해 주세요!"

[응답 예시]
- (좋음) "게시물 목록을 조회했습니다."
- (좋음) "프로필이 업데이트되었습니다."
- (좋음) "공고에 지원했습니다."""
        ))

        // 이전 대화 내역 추가
        messages.addAll(recentMessages.filter { it.content != null }.map { msg ->
            OpenAiMessage(
                role = msg.role,
                content = msg.content ?: "",
                toolCalls = null,
                toolCallId = null
            )
        })

        // 4. OpenAI API 호출 및 Function Calling 처리
        var continueLoop = true
        var finalResponse = ""
        var maxIterations = 10 // 무한 루프 방지
        val functionResults = mutableListOf<FunctionResult>() // 함수 실행 결과 수집

        while (continueLoop && maxIterations > 0) {
            maxIterations--

            val request = OpenAiChatRequest(
                model = "gpt-4o-mini",
                messages = messages,
                tools = AgentTools.tools,
                toolChoice = "auto",
                temperature = 0.7,
                maxTokens = 2000
            )

            val response = openAiService.createChatCompletion(request)
            val choice = response.choices.firstOrNull() ?: break

            val assistantMessage = choice.message

            // 5. Assistant 메시지 저장
            val assistantChatMessage = ChatMessage(
                sessionId = session.id,
                role = "assistant",
                content = assistantMessage.content,
                toolCalls = assistantMessage.toolCalls?.let { objectMapper.writeValueAsString(it) }
            )
            chatMessageRepository.save(assistantChatMessage)

            messages.add(assistantMessage)

            // 6. Function calling 확인
            if (assistantMessage.toolCalls != null && assistantMessage.toolCalls.isNotEmpty()) {
                // 함수 실행
                for (toolCall in assistantMessage.toolCalls) {
                    val functionName = toolCall.function.name
                    val functionArgs = toolCall.function.arguments

                    // 함수 실행
                    val functionResult = agentFunctionExecutor.executeFunction(
                        functionName,
                        functionArgs,
                        userId
                    )

                    // 함수 결과를 JSON 객체로 파싱해서 저장
                    try {
                        val resultData = objectMapper.readValue(functionResult, Any::class.java)
                        functionResults.add(FunctionResult(functionName, resultData))
                    } catch (e: Exception) {
                        functionResults.add(FunctionResult(functionName, mapOf("raw" to functionResult)))
                    }

                    // Tool 메시지 생성 및 저장
                    val toolMessage = OpenAiMessage(
                        role = "tool",
                        content = functionResult,
                        toolCallId = toolCall.id
                    )

                    val toolChatMessage = ChatMessage(
                        sessionId = session.id,
                        role = "tool",
                        content = functionResult,
                        toolCallId = toolCall.id,
                        functionName = functionName
                    )
                    chatMessageRepository.save(toolChatMessage)

                    messages.add(toolMessage)
                }
            } else {
                // 더 이상 함수 호출이 없으면 종료
                finalResponse = assistantMessage.content ?: ""
                continueLoop = false
            }
        }

        // 7. 세션 업데이트
        session.updatedAt = LocalDateTime.now()
        chatSessionRepository.save(session)

        return ChatResponse(
            sessionId = session.id,
            message = finalResponse,
            finished = true,
            functionResults = if (functionResults.isNotEmpty()) functionResults else null
        )
    }

    fun getSessions(userId: Long): List<ChatSessionSummary> {
        val sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
        return sessions.map {
            ChatSessionSummary(
                sessionId = it.id,
                title = it.title ?: "New Chat",
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
    }

    fun getSessionMessages(userId: Long, sessionId: Long): List<ChatMessageResponse> {
        val session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw IllegalArgumentException("Session not found or unauthorized")

        val messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
        return messages.filter { it.role == "user" || it.role == "assistant" }
            .map {
                ChatMessageResponse(
                    role = it.role,
                    content = it.content ?: "",
                    createdAt = it.createdAt
                )
            }
    }
}

// Response DTOs
data class FunctionResult(
    val functionName: String,
    val data: Any?
)

data class ChatResponse(
    val sessionId: Long,
    val message: String,
    val finished: Boolean,
    val functionResults: List<FunctionResult>? = null
)

data class ChatSessionSummary(
    val sessionId: Long,
    val title: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ChatMessageResponse(
    val role: String,
    val content: String,
    val createdAt: LocalDateTime
)