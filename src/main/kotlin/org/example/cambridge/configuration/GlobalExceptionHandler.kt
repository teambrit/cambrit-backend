package org.example.cambridge.configuration

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

// 모든 컨트롤러 전역에서 예외 처리
@ControllerAdvice
class GlobalExceptionHandler {

    // IllegalArgumentException → 400 Bad Request
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity(
            mapOf("message" to (ex.message ?: "잘못된 요청입니다.")),
            HttpStatus.BAD_REQUEST
        )
    }

    // 인증 관련 예외 → 401 Unauthorized
    @ExceptionHandler(SecurityException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleSecurityException(ex: SecurityException): ResponseEntity<Map<String, String>> {
        return ResponseEntity(
            mapOf("message" to (ex.message ?: "인증이 필요합니다.")),
            HttpStatus.UNAUTHORIZED
        )
    }

    // 그 외 모든 예외 → 500 Internal Server Error
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity(
            mapOf("message" to (ex.message ?: "서버 오류가 발생했습니다.")),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}