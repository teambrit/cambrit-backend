package org.example.cambridge.openai

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OpenAiApiLogRepository : JpaRepository<OpenAiApiLog, Long> {
}