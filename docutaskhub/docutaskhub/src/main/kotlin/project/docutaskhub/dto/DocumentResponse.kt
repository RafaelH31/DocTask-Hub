package project.docutaskhub.dto

data class DocumentResponse(
    val id: Int,
    val nome: String,
    val type: String,
    val taskId: Int
)