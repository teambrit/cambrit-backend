package org.example.cambridge.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "student_authorization_request")
class StudentAuthorizationRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0L,

    @Column(name = "university", nullable = true)
    val university: String? = null,

    @Column(name = "major", nullable = true)
    val major: String? = null,

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file", nullable = false, columnDefinition = "LONGBLOB")
    val file: ByteArray = ByteArray(0),

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: StudentAuthorizationStatus = StudentAuthorizationStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_modified_at", nullable = false)
    val lastModifiedAt: LocalDateTime = LocalDateTime.now()
)