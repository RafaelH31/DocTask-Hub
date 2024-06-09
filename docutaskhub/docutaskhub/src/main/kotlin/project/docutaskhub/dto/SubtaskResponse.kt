package project.docutaskhub.dto

import jdk.jshell.Snippet
import java.time.LocalDate
import java.time.LocalDateTime

data class SubtaskResponse(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val status: Snippet.Status,
    val cor: String?,
    val dataDeCriacao: LocalDateTime,
    val dataDeAtualizacao: LocalDateTime?,
    val dataDeVencimento: LocalDate?,
    val criadoPorId: Int,
    val atribuidoParaId: Int
)