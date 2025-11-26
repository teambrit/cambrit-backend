package org.example.cambridge.user

import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository:JpaRepository<User,Long> {
    fun findByEmailAndIsDeletedFalse(email:String):User?
}