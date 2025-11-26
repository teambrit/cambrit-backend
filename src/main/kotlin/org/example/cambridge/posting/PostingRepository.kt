package org.example.cambridge.posting

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostingRepository: JpaRepository<Posting,Long> {
    fun findAllByPosterId(posterId: Long,pageable: Pageable): Page<Posting>

    fun findAllBy(pageable: Pageable): Page<Posting>
}