package project.docutaskhub.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

class UserRequest (
    @field:NotBlank(message = "Nome de usuário é obrigatório")
    @field:Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
    val username: String,

    @field:NotBlank(message = "Email é obrigatório")
    @field:Email(message = "Email inválido")
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    val senha: String,

    var dataDeRegistro: LocalDateTime = LocalDateTime.now()
)