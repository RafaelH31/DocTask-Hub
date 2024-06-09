package project.docutaskhub.dto

import jakarta.validation.constraints.NotBlank

data class BoardAttRequest(
    @field:NotBlank(message = "Nome é obrigatório")
    val nome: String?,

    @field:NotBlank(message = "Descrição é obrigatória")
    val descricao: String?,

    val usuarios: List<String>?
)
