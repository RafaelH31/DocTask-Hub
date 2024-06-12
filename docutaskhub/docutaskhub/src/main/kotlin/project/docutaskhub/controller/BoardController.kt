package project.docutaskhub.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.data.repository.query.Param
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import project.docutaskhub.dto.BoardAttRequest
import project.docutaskhub.dto.BoardRequest
import project.docutaskhub.service.BoardService

@RestController
@RequestMapping("/boards")
class BoardController(private val boardService: BoardService) {

    @Operation(summary = "Criar um novo quadro", description = "Retorna os detalhes do quadro criado")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Quadro criado com sucesso"),
            ApiResponse(responseCode = "400", description = "Erro nos dados do quadro"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PostMapping("/criar")
    fun post(@RequestBody @Valid boardRequest: BoardRequest): ResponseEntity<Any> {
        return try {
            val createdBoard = boardService.criarBoard(boardRequest)
            ResponseEntity(createdBoard, HttpStatus.CREATED)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Listar todos os quadros de um usuário", description = "Retorna os quadros criados e associados ao usuário")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Lista de quadros obtida com sucesso"),
            ApiResponse(responseCode = "404", description = "Nenhum quadro encontrado para o usuário"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/buscar/{idUsuario}")
    fun getAll(@PathVariable idUsuario: Int): ResponseEntity<Any> {
        return try {
            val (boardsCriados, boardsAssociados) = boardService.buscarBoards(idUsuario)
            if (boardsCriados.isEmpty() && boardsAssociados.isEmpty()) {
                ResponseEntity.status(404).body(mapOf("message" to "Nenhum quadro encontrado para o usuário"))
            } else {
                val response = mapOf(
                    "boardsCriados" to boardsCriados,
                    "boardsAssociados" to boardsAssociados
                )
                ResponseEntity.ok(response)
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Visualizar detalhes de um quadro específico", description = "Retorna os detalhes de um quadro específico")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Detalhes do quadro obtidos com sucesso"),
            ApiResponse(responseCode = "404", description = "Quadro não encontrado"),
            ApiResponse(responseCode = "401", description = "Usuário não autorizado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/visualizar/{boardId}/{userId}")
    fun getById(
        @PathVariable userId: Int,
        @PathVariable boardId: Int
    ): ResponseEntity<Any> {
        return try {
            val boardDetails = boardService.visualizarBoard(userId, boardId)
            if (boardDetails != null) {
                ResponseEntity.ok(boardDetails)
            } else {
                ResponseEntity.status(401).body(mapOf("message" to "Usuário não autorizado a acessar o quadro"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Atualizar informações de um quadro", description = "Atualiza o nome, descrição e usuários associados ao quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Quadro atualizado com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
            ApiResponse(responseCode = "404", description = "Quadro não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PatchMapping("/atualizar/{boardId}")
    fun patch(
        @PathVariable boardId: Int,
        @RequestBody @Valid updateRequest: BoardAttRequest
    ): ResponseEntity<Any> {
        return try {
            val updatedBoard = boardService.atualizarBoard(boardId, updateRequest)
            ResponseEntity.ok(updatedBoard)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to "Quadro não encontrado"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Excluir um quadro", description = "Exclui um quadro e todos os seus itens associados")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Quadro excluído com sucesso"),
            ApiResponse(responseCode = "404", description = "Quadro não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @DeleteMapping("/deletar/{boardId}")
    fun delete(@PathVariable boardId: Int): ResponseEntity<Any> {
        return try {
            boardService.deletarBoard(boardId)
            ResponseEntity.status(204).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("message" to "Quadro não encontrado"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }


}
