package org.example.cambridge.openai

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.cambridge.openai.dto.OpenAiChatRequest
import org.example.cambridge.openai.dto.OpenAiChatResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OpenAiService(
    private val openAiClient: OpenAiClient,
    private val openAiApiLogRepository: OpenAiApiLogRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${openai.api-key}") private val apiKey: String
) {

    @Transactional
    fun createChatCompletion(request: OpenAiChatRequest): OpenAiChatResponse {
        // Convert request to JSON string
        val requestJson = objectMapper.writeValueAsString(request)

        // Call OpenAI API
        val response = openAiClient.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = request
        )

        // Convert response to JSON string
        val responseJson = objectMapper.writeValueAsString(response)

        // Save log
        val log = OpenAiApiLog(
            createdAt = LocalDateTime.now(),
            request = requestJson,
            response = responseJson
        )
        openAiApiLogRepository.save(log)

        return response
    }
}