package project.docutaskhub.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import project.docutaskhub.dto.LoginRequest
import project.docutaskhub.dto.UserRequest
import project.docutaskhub.service.UserService

@RestController
@RequestMapping("/users")
@Validated
class UserController (
    private val userService: UserService
) {

    @Operation(summary = "Cadastrar um novo usuário", description = "Retorna os detalhes do usuário cadastrado")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            ApiResponse(responseCode = "400", description = "Email já cadastrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PostMapping("/cadastrar")
    fun post(@RequestBody @Valid novoUsuario: UserRequest): ResponseEntity<Any> {
        return try {
            val userResponse = userService.cadastrarUsuario(novoUsuario)
            ResponseEntity.status(201).body(userResponse)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Login de usuário")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
            ApiResponse(responseCode = "400", description = "Credenciais inválidas")
        ]
    )
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        return if (userService.login(loginRequest)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(400).build()
        }
    }

    @Operation(summary = "Obter todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Lista de usuários obtida com sucesso"),
            ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/buscar")
    fun getAll(): ResponseEntity<Any> {
        return try {
            val users = userService.buscarAllUsuarios()
            if (users.isEmpty()) {
                ResponseEntity.status(404).body(mapOf("message" to "Nenhum usuário encontrado"))
            } else {
                ResponseEntity.ok(users)
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Obter um usuário pelo ID", description = "Retorna os detalhes de um usuário pelo seu ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @GetMapping("/buscar/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<Any> {
        return try {
            val user = userService.buscarUsuarioById(id)
            if (user != null) {
                ResponseEntity.ok(user)
            } else {
                ResponseEntity.status(404).body(mapOf("message" to "Usuário não encontrado"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Atualizar um usuário pelo ID", description = "Atualiza as informações de um usuário pelo seu ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Usuário atualizado com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados inválidos para atualização"),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @PatchMapping("/atualizar/{id}")
    fun patch(@PathVariable id: Int, @RequestBody @Valid userRequest: UserRequest): ResponseEntity<Any> {
        return try {
            userService.atualizarUsuario(id, userRequest)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to "Usuário não encontrado"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

    @Operation(summary = "Excluir um usuário pelo ID", description = "Exclui um usuário pelo seu ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    @DeleteMapping("/deletar/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Any> {
        return try {
            userService.deletarUsuario(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to "Usuário não encontrado"))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("message" to e.message))
        }
    }

}