package org.example.cambridge.openai.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class FunctionParameter(
    val type: String,
    val properties: Map<String, PropertyDefinition>,
    val required: List<String>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PropertyDefinition(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: FunctionParameter
)

data class Tool(
    val type: String = "function",
    val function: FunctionDefinition
)

data class FunctionCall(
    val name: String,
    val arguments: String
)

data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)
