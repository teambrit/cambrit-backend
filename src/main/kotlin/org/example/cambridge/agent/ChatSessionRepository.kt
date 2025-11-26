package org.example.cambridge.agent

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatSessionRepository : JpaRepository<ChatSession, Long> {
    fun findByUserIdOrderByUpdatedAtDesc(userId: Long): List<ChatSession>
    fun findByIdAndUserId(id: Long, userId: Long): ChatSession?
}