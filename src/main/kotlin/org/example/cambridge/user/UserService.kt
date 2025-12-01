package org.example.cambridge.user

import org.example.cambridge.configuration.JwtUtil
import org.example.cambridge.user.data.UserResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val studentAuthorizationRequestRepository: StudentAuthorizationRequestRepository
) {
    fun createUser(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ): String {
        val encodedPassword = passwordEncoder.encode(password)

        val existingUser = userRepository.findByEmailAndIsDeletedFalse(email)
        if(existingUser != null){
            throw IllegalArgumentException("User with email $email already exists")
        }

        val user = User(
            name = name,
            email = email,
            password = encodedPassword,
            role = role,
            isAuthorized = false
        )
        userRepository.save(user)
        return "User created successfully"
    }

    fun login(email: String, rawPassword: String,role: UserRole): String? {
        val user = userRepository.findByEmailAndIsDeletedFalse(email) ?: return null
        if(user.role != role) return null
        return if (passwordEncoder.matches(rawPassword, user.password)) {
            jwtUtil.generateToken(user.id.toString())
        } else {
            null
        }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("User not found")
        val authorizationRequest = if(user.role == UserRole.STUDENT) {
            studentAuthorizationRequestRepository.findAllByUserId(user.id).maxByOrNull { it.createdAt }
        } else null

        return UserResponse(
            name = user.name,
            email = user.email,
            role = user.role,
            isAuthorized = user.isAuthorized,
            id = user.id,
            logoImage = if(user.logoImage !=null) java.util.Base64.getEncoder().encodeToString (user.logoImage) else null,
            isDeleted = user.isDeleted,
            studentAuthorizationStatus = getStudentAuthorizationStatus(user.id),
            university = authorizationRequest?.university,
            major = authorizationRequest?.major,
            phoneNumber = user.phoneNumber,
            profileImage = if(user.profileImage !=null) java.util.Base64.getEncoder().encodeToString(user.profileImage) else null,
            description = user.description,
            companyUrl = user.companyUrl,
            companyCode = user.companyCode,
            backgroundImage = if(user.backgroundImage != null) java.util.Base64.getEncoder().encodeToString(user.backgroundImage) else null,
            bankNumber = user.bankNumber,
            bankName = user.bankName
        )
    }

    fun requestStudentAuthorization(userId: Long, university: String, major: String, file: ByteArray){
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")
        if (user.role != UserRole.STUDENT) {
            throw IllegalArgumentException("Only students can request authorization")
        }
        if (user.isAuthorized) {
            throw IllegalArgumentException("User is already authorized")
        }
        // Check if there's already a pending request
        val existingRequests = studentAuthorizationRequestRepository.findAllByUserId(userId)
        if (existingRequests.any { it.status == StudentAuthorizationStatus.PENDING }) {
            throw IllegalArgumentException("There is already a pending authorization request for this user")
        }
        if(existingRequests.any { it.status == StudentAuthorizationStatus.APPROVED }){
            throw IllegalArgumentException("This user is already approved")
        }

        val request = StudentAuthorizationRequest(
            userId = userId,
            university = university,
            major = major,
            file = file,
            status = StudentAuthorizationStatus.PENDING,
            createdAt = java.time.LocalDateTime.now(),
            lastModifiedAt = java.time.LocalDateTime.now()
        )
        studentAuthorizationRequestRepository.save(request)
    }

    fun getStudentAuthorizationStatus(userId: Long): StudentAuthorizationStatus {
        val requests =  studentAuthorizationRequestRepository.findAllByUserId(userId)

        val recentRequest = requests.maxByOrNull { it.createdAt } ?: return StudentAuthorizationStatus.NONE
        return recentRequest.status
    }

    fun getAllStudents(): List<UserResponse> {
        val students = userRepository.findAll().filter { it.role == UserRole.STUDENT}

        val authorizationMap = studentAuthorizationRequestRepository.findAll()
            .groupBy { it.userId }
            .mapValues { entry -> entry.value.maxByOrNull { it.createdAt }}

        return students.map {
            UserResponse(
                name = it.name,
                email = it.email,
                role = it.role,
                isAuthorized = it.isAuthorized,
                id = it.id,
                logoImage = null,
                isDeleted = it.isDeleted,
                studentAuthorizationStatus = authorizationMap[it.id]?.status ?: StudentAuthorizationStatus.NONE,
                university = authorizationMap[it.id]?.university,
                major = authorizationMap[it.id]?.major,
                phoneNumber = it.phoneNumber,
                profileImage = if(it.profileImage != null) java.util.Base64.getEncoder().encodeToString(it.profileImage) else null,
                description = it.description,
                companyUrl = null,
                companyCode = null,
                backgroundImage = null,
                bankNumber = it.bankNumber,
                bankName = it.bankName
            )
        }
    }

    fun getStudentAuthorizationFile(userId: Long):ByteArray{
        val requests =  studentAuthorizationRequestRepository.findAllByUserId(userId)

        val recentRequest = requests.maxByOrNull { it.createdAt } ?: throw IllegalArgumentException("No authorization request found for user with id $userId")
        return recentRequest.file
    }


    @Transactional
    fun reviewStudentAuthorizationRequest(userId: Long, approve: Boolean){
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")
        if (user.role != UserRole.STUDENT) {
            throw IllegalArgumentException("Only students can have authorization requests")
        }
        val requests =  studentAuthorizationRequestRepository.findAllByUserId(userId)
        val recentRequest = requests.filter { it.status == StudentAuthorizationStatus.PENDING }.maxByOrNull { it.createdAt } ?: throw IllegalArgumentException("No pending authorization request found for user with id $userId")

        recentRequest.status = if(approve) StudentAuthorizationStatus.APPROVED else StudentAuthorizationStatus.REJECTED

        studentAuthorizationRequestRepository.save(recentRequest)

        if(approve){
            user.isAuthorized = true
            userRepository.save(user)
        }
    }

    @Transactional
    fun updateUserProfile(
        userId: Long,
        name: String,
        phoneNumber: String?,
        description: String?,
        profileImage: ByteArray?,
        bankNumber: String?,
        bankName: String?
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")

        user.name = name
        user.phoneNumber = phoneNumber
        user.description = description
        user.bankNumber = bankNumber
        user.bankName = bankName

        if (profileImage != null) {
            user.profileImage = profileImage
        }

        userRepository.save(user)
    }

    @Transactional
    fun updateCompanyProfile(
        userId: Long,
        name: String,
        companyCode: String?,
        companyUrl: String?,
        description: String?,
        logoImage: ByteArray?,
        backgroundImage: ByteArray?,
        bankNumber: String?,
        bankName: String?
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")

        if (user.role != UserRole.COMPANY) {
            throw IllegalArgumentException("Only companies can update company profile")
        }

        user.name = name
        user.companyCode = companyCode
        user.companyUrl = companyUrl
        user.description = description
        user.bankNumber = bankNumber
        user.bankName = bankName

        if (logoImage != null) {
            user.logoImage = logoImage
        }

        if (backgroundImage != null) {
            user.backgroundImage = backgroundImage
        }

        userRepository.save(user)
    }
}