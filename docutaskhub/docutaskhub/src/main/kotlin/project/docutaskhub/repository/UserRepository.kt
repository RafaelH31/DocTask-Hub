package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.User

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun existsByEmail(email: String): Boolean
}