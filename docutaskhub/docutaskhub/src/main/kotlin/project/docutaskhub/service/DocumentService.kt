package project.docutaskhub.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import project.docutaskhub.dominio.Document
import project.docutaskhub.dto.DocumentResponse
import project.docutaskhub.enums.DocumentType
import project.docutaskhub.repository.DocumentRepository
import project.docutaskhub.repository.TaskRepository
import java.io.IOException

@Service
class DocumentService (
    private val documentRepository: DocumentRepository,
    private val taskRepository: TaskRepository
){

    @Transactional
    fun enviarDocumento(taskId: Int, file: MultipartFile, documentType: DocumentType): Document {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Tarefa não encontrada com o ID $taskId") }


        if (file.isEmpty) {
            throw IllegalArgumentException("Arquivo vazio não é permitido")
        }

        try {
            val document = Document(
                nome = file.originalFilename ?: "Documento",
                arquivo = file.bytes,
                type = documentType,
                task = task
            )

            return documentRepository.save(document)
        } catch (e: IOException) {
            throw IllegalArgumentException("Falha ao ler o arquivo")
        }
    }

    fun buscarTipo(file: MultipartFile): DocumentType {
        val contentType = file.contentType ?: throw IllegalArgumentException("Tipo de arquivo inválido")
        return when {
            contentType.startsWith("image") -> DocumentType.IMAGE
            contentType == "application/pdf" -> DocumentType.PDF
            contentType == "text/plain" -> DocumentType.TXT
            contentType == "text/csv" -> DocumentType.CSV
            else -> DocumentType.OTHER
        }
    }

    @Transactional(readOnly = true)
    fun listarTodosOsDocumentos(boardId: Int): List<DocumentResponse> {
        val documents = documentRepository.findAllByTaskBoardId(boardId)
        return documents.map { mapToDocumentResponse(it) }
    }

    private fun mapToDocumentResponse(document: Document): DocumentResponse {
        return DocumentResponse(
            id = document.id,
            nome = document.nome,
            type = document.type,
            taskId = document.task.id
        )
    }

    fun getDocument(boardId: Int, documentId: Int): Document {
        return documentRepository.findByIdAndTaskBoardId(documentId, boardId)
            .orElseThrow { IllegalArgumentException("Documento não encontrado com ID $documentId no quadro com ID $boardId") }
    }

    fun deletarDocumento(boardId: Int, documentId: Int) {
        val document = documentRepository.findByIdAndTaskBoardId(documentId, boardId)
            .orElseThrow { IllegalArgumentException("Documento não encontrado com o ID $documentId no quadro com o ID $boardId") }

        documentRepository.delete(document)
    }
}