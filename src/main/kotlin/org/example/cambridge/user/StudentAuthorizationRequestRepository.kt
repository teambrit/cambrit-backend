package org.example.cambridge.user

import org.springframework.data.jpa.repository.JpaRepository

interface StudentAuthorizationRequestRepository: JpaRepository<StudentAuthorizationRequest, Long> {
    fun findAllByUserId(userId: Long): List<StudentAuthorizationRequest>
}