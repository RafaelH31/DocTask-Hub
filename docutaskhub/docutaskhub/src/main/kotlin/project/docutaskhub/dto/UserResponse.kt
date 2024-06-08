package project.docutaskhub.dto

import java.time.LocalDateTime

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val dataDeRegistro: LocalDateTime
)