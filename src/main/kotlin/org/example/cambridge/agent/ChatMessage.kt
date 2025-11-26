package org.example.cambridge.agent

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "session_id", nullable = false)
    val sessionId: Long = 0,

    @Column(name = "role", nullable = false, length = 50)
    val role: String = "user", // "user", "assistant", "tool", "system"

    @Column(name = "content", columnDefinition = "LONGTEXT")
    val content: String? = null,

    @Column(name = "tool_calls", columnDefinition = "LONGTEXT")
    val toolCalls: String? = null, // JSON string

    @Column(name = "tool_call_id", length = 200)
    val toolCallId: String? = null,

    @Column(name = "function_name", length = 100)
    val functionName: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)