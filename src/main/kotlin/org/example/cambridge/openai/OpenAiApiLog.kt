package org.example.cambridge.openai

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "openai_api_log")
class OpenAiApiLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "created_at", nullable = true)
    var createdAt: LocalDateTime? = LocalDateTime.now(),

    @Lob
    @Column(name = "request", nullable = true, columnDefinition = "TEXT")
    var request: String? = null,

    @Lob
    @Column(name = "response", nullable = true, columnDefinition = "TEXT")
    var response: String? = null
)