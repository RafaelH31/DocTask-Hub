package project.docutaskhub.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import project.docutaskhub.dto.TaskRequest
import project.docutaskhub.service.TaskService

@RestController
@RequestMapping("/boards/{boardId}/tasks")
class TaskController(
    private val taskService: TaskService
) {
    @Operation(summary = "Adicionar uma nova tarefa a um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Task adicionada com sucesso"),
            ApiResponse(responseCode = "400", description = "Requisição inválida"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PostMapping("/criar")
    fun post(
        @PathVariable boardId: Int,
        @RequestBody taskRequest: TaskRequest
    ): ResponseEntity<Any> {
        return try {
            val newTask = taskService.criarTask(boardId, taskRequest)
            ResponseEntity.status(201).body(newTask)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Buscar uma tarefa específica de um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/visualizar/{taskId}")
    fun getById(
        @PathVariable boardId: Int,
        @PathVariable taskId: Int
    ): ResponseEntity<Any> {
        return try {
            val task = taskService.visualizarTask(boardId, taskId)
            ResponseEntity.ok(task)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Buscar todas as tarefas associadas a um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tarefas encontradas"),
            ApiResponse(responseCode = "404", description = "Quadro não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping
    fun getAll(
        @PathVariable boardId: Int,
        @Parameter(description = "ID do usuário para filtrar as tarefas atribuídas")
        @RequestParam(required = false) userId: Int?
    ): ResponseEntity<Any> {
        return try {
            val tasks = if (userId != null) {
                taskService.getTasksByBoardAndUser(boardId, userId)
            } else {
                taskService.getAllTasksFromBoard(boardId)
            }
            ResponseEntity.ok(tasks)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Atualizar informações de uma tarefa")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            ApiResponse(responseCode = "400", description = "Requisição inválida"),
            ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PutMapping("/atualizar/{taskId}")
    fun update(
        @PathVariable boardId: Int,
        @PathVariable taskId: Int,
        @RequestBody taskRequest: TaskRequest
    ): ResponseEntity<Any> {
        return try {
            val updatedTask = taskService.updateTask(boardId, taskId, taskRequest)
            ResponseEntity.ok(updatedTask)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

    @Operation(summary = "Excluir uma tarefa específica de um quadro")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
            ApiResponse(responseCode = "400", description = "Requisição inválida"),
            ApiResponse(responseCode = "403", description = "Usuário não tem permissão para excluir a tarefa"),
            ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @DeleteMapping("/deletar/{taskId}")
    fun delete(
        @PathVariable boardId: Int,
        @PathVariable taskId: Int,
        @RequestParam userId: Int
    ): ResponseEntity<Any> {
        return try {
            taskService.deleteTask(userId, boardId, taskId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to e.message))
        }
    }

}