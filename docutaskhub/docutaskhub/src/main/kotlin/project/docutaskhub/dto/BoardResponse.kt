package project.docutaskhub.dto

import org.springframework.scheduling.config.Task

data class BoardResponse(
    val id: Int,
    val nome: String,
    val descricao: String,
    val criadoPorId: Int,
    val usuarios: List<UserResponse>?,
    val tasks: List<Task>?
)
