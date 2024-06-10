package project.docutaskhub.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.LoginRequest
import project.docutaskhub.dto.UserRequest
import project.docutaskhub.repository.UserRepository
import java.util.*


class UserServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val userService: UserService = UserService(userRepository)

    @Test
    fun `deve cadastrar um novo usuário com sucesso`() {
        val novoUsuario = UserRequest("usuarioTeste", "teste@example.com", "senha123")
        val userComId = User(
            id = 1,
            username = novoUsuario.username,
            email = novoUsuario.email,
            senha = novoUsuario.senha,
            dataDeRegistro = novoUsuario.dataDeRegistro
        )

        `when`(userRepository.existsByEmail(novoUsuario.email)).thenReturn(false)
        `when`(userRepository.save(any(User::class.java))).thenReturn(userComId)

        val userResponse = userService.cadastrarUsuario(novoUsuario)

        assertEquals(novoUsuario.username, userResponse.username)
        assertEquals(novoUsuario.email, userResponse.email)
    }


    @Test
    fun `deve lançar erro quando o email já existe`() {
        val novoUsuario = UserRequest("usuarioTeste", "teste@example.com", "senha123")

        `when`(userRepository.existsByEmail(novoUsuario.email)).thenReturn(true)

        val exception = assertThrows<ResponseStatusException> {
            userService.cadastrarUsuario(novoUsuario)
        }

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST)
        assertEquals("Email já cadastrado", exception.reason)
    }

    @Test
    fun `deve retornar verdadeiro para login válido`() {
        val loginRequest = LoginRequest("teste@example.com", "senha123")
        val user = User("usuarioTeste", "teste@example.com", "senha123")

        `when`(userRepository.findByEmailAndSenha(loginRequest.email, loginRequest.senha)).thenReturn(user)

        val resultado = userService.login(loginRequest)

        assertTrue(resultado)
    }

    @Test
    fun `deve retornar falso para login inválido`() {
        val loginRequest = LoginRequest("teste@example.com", "senhaErrada")

        `when`(userRepository.findByEmailAndSenha(loginRequest.email, loginRequest.senha)).thenReturn(null)

        val resultado = userService.login(loginRequest)

        assertFalse(resultado)
    }

    @Test
    fun `deve retornar todos os usuários`() {
        val usuarios = listOf(
            User(1, "usuario1", "usuario1@example.com", "senha1"),
            User(2, "usuario2", "usuario2@example.com", "senha2")
        )

        `when`(userRepository.findAll()).thenReturn(usuarios)

        val userResponses = userService.buscarAllUsuarios()

        assertEquals(2, userResponses.size)
        assertEquals("usuario1", userResponses[0].username)
        assertEquals("usuario2", userResponses[1].username)
    }

    @Test
    fun `deve retornar usuário por id`() {
        val usuario = User("usuarioTeste", "teste@example.com", "senha123")
        usuario.id = 1

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))

        val userResponse = userService.buscarUsuarioById(1)

        assertNotNull(userResponse)
        assertEquals("usuarioTeste", userResponse?.username)
    }

    @Test
    fun `deve retornar nulo para id de usuário não existente`() {
        `when`(userRepository.findById(1)).thenReturn(Optional.empty())

        val userResponse = userService.buscarUsuarioById(1)

        assertNull(userResponse)
    }

    @Test
    fun `deve atualizar usuário com sucesso`() {
        val usuario = User("usuarioTeste", "teste@example.com", "senha123")
        usuario.id = 1

        val usuarioAtualizadoRequest = UserRequest("usuarioAtualizado", "atualizado@example.com", "novasenha123")

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))
        `when`(userRepository.save(any(User::class.java))).thenAnswer { it.arguments[0] }

        userService.atualizarUsuario(1, usuarioAtualizadoRequest)

        assertEquals("usuarioAtualizado", usuario.username)
        assertEquals("atualizado@example.com", usuario.email)
        assertEquals("novasenha123", usuario.senha)
    }

    @Test
    fun `deve lançar erro ao atualizar usuário não existente`() {
        val usuarioAtualizadoRequest = UserRequest("usuarioAtualizado", "atualizado@example.com", "novasenha123")

        `when`(userRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<NoSuchElementException> {
            userService.atualizarUsuario(1, usuarioAtualizadoRequest)
        }

        assertEquals("Usuário não encontrado", exception.message)
    }

    @Test
    fun `deve deletar usuário com sucesso`() {
        `when`(userRepository.existsById(1)).thenReturn(true)
        doNothing().`when`(userRepository).deleteById(1)

        userService.deletarUsuario(1)

        verify(userRepository, times(1)).deleteById(1)
    }

    @Test
    fun `deve lançar erro ao deletar usuário não existente`() {
        `when`(userRepository.existsById(1)).thenReturn(false)

        val exception = assertThrows<NoSuchElementException> {
            userService.deletarUsuario(1)
        }

        assertEquals("Usuário não encontrado", exception.message)
    }
}
