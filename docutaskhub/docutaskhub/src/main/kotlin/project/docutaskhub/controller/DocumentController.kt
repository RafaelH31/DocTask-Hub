package project.docutaskhub.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import project.docutaskhub.dto.DocumentResponse
import project.docutaskhub.enums.DocumentType
import project.docutaskhub.service.DocumentService

@RestController
@RequestMapping("/boards/{boardId}/documents")
class DocumentController(
    private val documentService: DocumentService
) {

    @Operation(summary = "Upload de um novo documento para um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Documento adicionado com sucesso"),
            ApiResponse(responseCode = "400", description = "Requisição inválida"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PostMapping("/upload")
    fun post(
        @RequestParam taskId: Int,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<Any> {
        return try {
            val documentType = documentService.buscarTipo(file)
            val document = documentService.enviarDocumento(taskId, file, documentType)
            ResponseEntity.ok(document)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Listar todos os documentos associados a um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Documentos encontrados"),
            ApiResponse(responseCode = "404", description = "Quadro não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping
    fun getAll(@PathVariable boardId: Int): ResponseEntity<List<DocumentResponse>> {
        return try {
            val documents = documentService.listarTodosOsDocumentos(boardId)
            ResponseEntity.ok(documents)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }

    @Operation(summary = "Baixar um documento específico")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Documento baixado com sucesso"),
            ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/baixar/{documentId}")
    fun get(
        @PathVariable boardId: Int,
        @PathVariable documentId: Int,
        request: HttpServletRequest
    ): ResponseEntity<ByteArrayResource> {
        val document = documentService.getDocument(boardId, documentId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.contentDisposition = ContentDisposition.builder("attachment")
            .filename(document.nome)
            .build()
        headers.contentLength = document.arquivo.size.toLong()

        val documentoBaixado = ByteArrayResource(document.arquivo)

        return ResponseEntity(documentoBaixado, headers, HttpStatus.OK)
    }

    @Operation(summary = "Excluir um documento específico")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Documento excluído com sucesso"),
            ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @DeleteMapping("/excluir/{documentId}")
    fun delete(
        @PathVariable boardId: Int,
        @PathVariable documentId: Int
    ): ResponseEntity<Any> {
        return try {
            documentService.deletarDocumento(boardId, documentId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        }
    }

}