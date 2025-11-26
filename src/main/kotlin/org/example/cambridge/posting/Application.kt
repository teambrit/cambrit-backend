package org.example.cambridge.posting
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "application")
class Application(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "applicant_id", nullable = false)
    var applicantId: Long = 0L,

    @Column(name = "posting_id", nullable = false)
    var postingId: Long = 0L,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "verification_file", nullable = true, columnDefinition = "LONGBLOB")
    var verificationFile: ByteArray? = null,

    @Column(name = "billing_id", nullable = false)
    var billingId: Long? = null,

    @Column(name = "charged_date", nullable = true)
    var chargedDate: LocalDateTime? = null
)