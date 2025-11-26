package org.example.cambridge.user

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Table(name = "user")
@Entity
class User(
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    val id: Long = 0,
    var name: String = "",
    val email: String ="",
    @Enumerated(jakarta.persistence.EnumType.STRING)
    val role: UserRole = UserRole.STUDENT,
    val password: String = "",
    var isAuthorized: Boolean = true,
    val isDeleted: Boolean = false,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @UpdateTimestamp
    @Column(name = "last_modified_at", nullable = false)
    val lastModifiedAt: LocalDateTime = LocalDateTime.now(),
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "logo_image", nullable = true, columnDefinition = "LONGBLOB")
    var logoImage: ByteArray? = null,
    @Column(name = "phone_number", nullable = true)
    var phoneNumber: String? = null,
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_image", nullable = true, columnDefinition = "LONGBLOB")
    var profileImage: ByteArray? = null,
    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "company_url", nullable = true)
    var companyUrl: String? = null,
    @Column(name = "company_code", nullable = true)
    var companyCode: String? = null,
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "background_image", nullable = true, columnDefinition = "LONGBLOB")
    var backgroundImage: ByteArray? = null
)