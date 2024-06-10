package project.docutaskhub.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.LoginRequest
import project.docutaskhub.dto.UserRequest
import project.docutaskhub.dto.UserResponse
import project.docutaskhub.repository.UserRepository

@Service
class UserService(private val userRepository: UserRepository) {

    fun cadastrarUsuario(novoUsuario: UserRequest): UserResponse {

        if (userRepository.existsByEmail(novoUsuario.email)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado")
        }

        val user = User(
            username = novoUsuario.username,
            email = novoUsuario.email,
            senha = novoUsuario.senha,
            dataDeRegistro = novoUsuario.dataDeRegistro
        )
        val usuarioSalvo = userRepository.save(user)
        return UserResponse(usuarioSalvo.id!!, usuarioSalvo.username, usuarioSalvo.email, usuarioSalvo.dataDeRegistro)
    }

    fun login(loginRequest: LoginRequest): Boolean {
        val user = userRepository.findByEmailAndSenha(loginRequest.email, loginRequest.senha)
        return user != null
    }

    fun buscarAllUsuarios(): List<UserResponse> {
        val users = userRepository.findAll()
        return users.map { it.toUserResponse() }
    }

    fun buscarUsuarioById(id: Int): UserResponse? {
        val user = userRepository.findById(id)
        return user.orElse(null)?.toUserResponse()
    }

    fun atualizarUsuario(id: Int, userRequest: UserRequest) {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("Usuário não encontrado") }
        user.apply {
            username = userRequest.username
            email = userRequest.email
            senha = userRequest.senha
        }
        userRepository.save(user)
    }

    fun deletarUsuario(id: Int) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("Usuário não encontrado")
        }
        userRepository.deleteById(id)
    }

    private fun User.toUserResponse(): UserResponse {
        val userId = id ?: throw IllegalStateException("ID do usuário não pode ser nulo")
        return UserResponse(userId, username, email, dataDeRegistro)
    }

}