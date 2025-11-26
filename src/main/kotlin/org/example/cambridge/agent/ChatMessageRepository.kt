package org.example.cambridge.agent

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findBySessionIdOrderByCreatedAtAsc(sessionId: Long): List<ChatMessage>
}