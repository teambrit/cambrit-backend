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
                description = """게시물 목록을 조회합니다.

이 함수는 전체 공고 목록을 반환합니다. 사용자가 특정 조건(기업명, 공고 제목, 보상액 등)으로 필터링을 요청하면:
1. 이 함수로 전체 목록을 먼저 조회하세요
2. 반환된 결과에서 조건에 맞는 항목들을 찾으세요
3. 조건에 맞는 항목의 개수와 간단한 설명만 사용자에게 알려주세요

예시:
- "삼성 공고 보여줘" → 전체 조회 후 posterName에 "삼성"이 포함된 항목 필터링
- "보상금 100만원 이상" → 전체 조회 후 compensation >= 1000000 항목 필터링
- "개발 관련 공고" → 전체 조회 후 title이나 tags에 "개발" 포함된 항목 필터링""",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "page" to PropertyDefinition(
                            type = "number",
                            description = "페이지 번호 (0부터 시작, 기본값: 0)"
                        ),
                        "size" to PropertyDefinition(
                            type = "number",
                            description = "페이지 크기 (기본값: 20, 필터링 시에는 50-100 권장)"
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
                name = "filter_postings",
                description = """필터링된 공고 목록을 반환합니다.

get_posting_list로 조회한 후 조건에 맞는 항목을 찾았다면, 해당 항목들의 ID를 이 함수에 전달하세요.
이 함수가 반환하는 결과가 사용자에게 최종적으로 표시됩니다.

사용 순서:
1. get_posting_list로 전체 목록 조회
2. 조건에 맞는 항목의 ID 리스트 추출 
3. filter_postings에 ID 리스트를 전달""",
                parameters = FunctionParameter(
                    type = "object",
                    properties = mapOf(
                        "postingIds" to PropertyDefinition(
                            type = "array",
                            description = "필터링된 공고 ID 목록",
                            items = mapOf("type" to "number")
                        )
                    ),
                    required = listOf("postingIds")
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
                name = "get_my_postings",
                description = "현재 로그인한 기업이 등록한 공고 목록을 조회합니다. (기업 전용)",
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
                        )
                    ),
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