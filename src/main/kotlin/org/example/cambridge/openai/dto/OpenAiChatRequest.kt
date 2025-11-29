package org.example.cambridge.openai.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    @JsonProperty("tool_calls")
    val toolCalls: List<ToolCall>? = null,
    @JsonProperty("tool_call_id")
    val toolCallId: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiChatRequest(
    val model: String = "gpt-5-mini",
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.1,
    @JsonProperty("max_completion_tokens")
    val maxTokens: Int = 1000,
    val tools: List<Tool>? = null,
    @JsonProperty("tool_choice")
    val toolChoice: String? = null
)