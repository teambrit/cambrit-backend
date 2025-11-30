package org.example.cambridge.posting
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "posting")
class Posting(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "poster_id", nullable = false)
    val posterId: Long = 0L,

    // MySQL TEXT 컬럼 매핑
    @Column(columnDefinition = "TEXT", nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT", nullable = false)
    var body: String = "",

    @Column(nullable = false)
    var compensation: Long = 0L,

    @Column(length = 255, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PostingStatus = PostingStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: LocalDateTime? = null,

    var applyDueDate: LocalDate? = null,
    var activityStartDate: LocalDate? = null,
    var activityEndDate: LocalDate? = null,
    var tags: String? = null,

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = true)
    var file: ByteArray? = null
)