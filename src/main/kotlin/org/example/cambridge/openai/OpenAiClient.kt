package org.example.cambridge.openai

import org.example.cambridge.openai.dto.OpenAiChatRequest
import org.example.cambridge.openai.dto.OpenAiChatResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "openai-client",
    url = "\${openai.api.url:https://api.openai.com}",
    configuration = [OpenAiClientConfig::class]
)
interface OpenAiClient {

    @PostMapping("/v1/chat/completions")
    fun createChatCompletion(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: OpenAiChatRequest
    ): OpenAiChatResponse
}