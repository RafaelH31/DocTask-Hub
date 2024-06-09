package project.docutaskhub.dto

import project.docutaskhub.enums.DocumentType

data class DocumentResponse(
    val id: Int,
    val nome: String,
    val type: DocumentType,
    val taskId: Int
)