package project.docutaskhub.dto

import project.docutaskhub.dominio.User
import project.docutaskhub.enums.Status
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskResponse(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val status: Status,
    val cor: String?,
    val dataDeCriacao: LocalDateTime,
    val dataDeAtualizacao: LocalDateTime?,
    val dataDeVencimento: LocalDate?,
    val criadoPorId: UserResponse,
    val atribuidoParaId: UserResponse,
    val documentos: List<DocumentResponse>
)