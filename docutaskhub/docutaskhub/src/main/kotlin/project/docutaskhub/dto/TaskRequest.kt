package project.docutaskhub.dto

import project.docutaskhub.enums.Status
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskRequest(
    val id: Int?,
    val titulo: String,
    val descricao: String,
    val status: Status,
    val cor: String?,
    val dataDeVencimento: LocalDate?,
    val dataDeCriacao: LocalDateTime = LocalDateTime.now(),
    val criadoPorId: Int,
    val atribuidoParaId: Int
)
