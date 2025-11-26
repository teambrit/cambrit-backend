package org.example.cambridge.agent

import org.example.cambridge.openai.dto.FunctionDefinition
import org.example.cambridge.openai.dto.FunctionParameter
import org.example.cambridge.openai.dto.PropertyDefinition
import org.example.cambridge.openai.dto.Tool

object AgentTools {
    val tools = listOf(
        Tool(
            function = FunctionDefinition(
                name = "get_user_info",
                description = "사용자 정보를 조회합니다. userId를 지정하지 않으면 현재 로그인한 사용자의 정보를 조회합니다.",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "userId" to PropertyDefinition(
                            type = "number",
                            description = "조회할 사용자 ID (선택사항, 없으면 본인 정보 조회)"
                        )
                    ),
                    required = emptyList()
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "update_user_profile",
                description = "사용자(학생) 프로필을 업데이트합니다. 이름, 전화번호, 자기소개를 수정할 수 있습니다.",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "name" to PropertyDefinition(
                            type = "string",
                            description = "사용자 이름"
                        ),
                        "phoneNumber" to PropertyDefinition(
                            type = "string",
                            description = "전화번호 (선택사항)"
                        ),
                        "description" to PropertyDefinition(
                            type = "string",
                            description = "자기소개 (선택사항)"
                        )
                    ),
                    required = listOf("name")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "update_company_profile",
                description = "기업 프로필을 업데이트합니다. 회사명, 사업자번호, 홈페이지, 소개를 수정할 수 있습니다.",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "name" to PropertyDefinition(
                            type = "string",
                            description = "회사명"
                        ),
                        "companyCode" to PropertyDefinition(
                            type = "string",
                            description = "사업자번호 (선택사항)"
                        ),
                        "companyUrl" to PropertyDefinition(
                            type = "string",
                            description = "회사 홈페이지 URL (선택사항)"
                        ),
                        "description" to PropertyDefinition(
                            type = "string",
                            description = "회사 소개 (선택사항)"
                        )
                    ),
                    required = listOf("name")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_posting_list",
                description = "게시물 목록을 조회합니다. 페이지네이션을 지원하며, 특정 기업의 게시물만 필터링할 수 있습니다.",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "page" to PropertyDefinition(
                            type = "number",
                            description = "페이지 번호 (0부터 시작, 기본값: 0)"
                        ),
                        "size" to PropertyDefinition(
                            type = "number",
                            description = "페이지 크기 (기본값: 20)"
                        ),
                        "posterId" to PropertyDefinition(
                            type = "number",
                            description = "특정 기업의 게시물만 조회 (선택사항)"
                        )
                    ),
                    required = emptyList()
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_posting_detail",
                description = "특정 게시물의 상세 정보를 조회합니다.",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "postingId" to PropertyDefinition(
                            type = "number",
                            description = "조회할 게시물의 ID"
                        )
                    ),
                    required = listOf("postingId")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_my_applications",
                description = "현재 로그인한 사용자의 지원 내역을 조회합니다. (학생 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = emptyMap(),
                    required = emptyList()
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_applications_for_posting",
                description = "특정 게시물에 대한 지원자 목록을 조회합니다. (기업 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "postingId" to PropertyDefinition(
                            type = "number",
                            description = "조회할 게시물의 ID"
                        )
                    ),
                    required = listOf("postingId")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_billing_list",
                description = "현재 로그인한 기업의 청구 목록을 조회합니다. (기업 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = emptyMap(),
                    required = emptyList()
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "get_billing_detail",
                description = "특정 청구서의 상세 정보를 조회합니다. (기업 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "billingId" to PropertyDefinition(
                            type = "number",
                            description = "조회할 청구서의 ID"
                        )
                    ),
                    required = listOf("billingId")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "create_posting",
                description = "새로운 공고를 생성합니다. (기업 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "title" to PropertyDefinition(
                            type = "string",
                            description = "공고 제목"
                        ),
                        "body" to PropertyDefinition(
                            type = "string",
                            description = "공고 내용"
                        ),
                        "compensation" to PropertyDefinition(
                            type = "number",
                            description = "보상금 (원 단위)"
                        ),
                        "tags" to PropertyDefinition(
                            type = "string",
                            description = "태그 (쉼표로 구분, 예: 'IT,개발,백엔드')"
                        )
                    ),
                    required = listOf("title", "body", "compensation")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "apply_to_posting",
                description = "특정 공고에 지원합니다. (학생 전용)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "postingId" to PropertyDefinition(
                            type = "number",
                            description = "지원할 공고의 ID"
                        )
                    ),
                    required = listOf("postingId")
                )
            )
        ),
        Tool(
            function = FunctionDefinition(
                name = "update_application_status",
                description = "지원자의 상태를 변경합니다. (기업 전용, 자신의 공고에만 가능)",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "applicationId" to PropertyDefinition(
                            type = "number",
                            description = "지원 내역 ID"
                        ),
                        "status" to PropertyDefinition(
                            type = "string",
                            description = "변경할 상태",
                            enum = listOf("PENDING", "APPROVED", "REJECTED", "VERIFIED")
                        )
                    ),
                    required = listOf("applicationId", "status")
                )
            )
        )
    )
}