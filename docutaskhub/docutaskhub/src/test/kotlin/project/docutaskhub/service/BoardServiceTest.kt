import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.BoardRequest
import project.docutaskhub.repository.BoardRepository
import project.docutaskhub.repository.TaskRepository
import project.docutaskhub.repository.UserRepository
import project.docutaskhub.service.BoardService
import java.util.*

class BoardServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val taskRepository: TaskRepository = mock(TaskRepository::class.java)
    private val boardRepository: BoardRepository = mock(BoardRepository::class.java)
    private val boardService: BoardService = BoardService(boardRepository, userRepository, taskRepository)

    @Test
    fun `deve criar um novo board com sucesso`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val boardRequest = BoardRequest("Novo Board", "Descrição do Board", usuario.id!!)

        `when`(userRepository.findById(boardRequest.fkUsuario)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.save(any(Board::class.java))).thenAnswer { it.arguments[0] }

        val boardResponse = boardService.criarBoard(boardRequest)

        assertEquals(boardRequest.nome, boardResponse.nome)
        assertEquals(boardRequest.descricao, boardResponse.descricao)
        assertEquals(usuario.id, boardResponse.criadoPorId)
    }

    @Test
    fun `deve lançar exceção ao tentar criar um board com usuário inexistente`() {
        val boardRequest = BoardRequest("Novo Board", "Descrição do Board", 1)

        `when`(userRepository.findById(boardRequest.fkUsuario)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            boardService.criarBoard(boardRequest)
        }

        assertEquals("Usuário não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve buscar boards por usuário`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board1 = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())
        val board2 = Board(2, "Board 2", "Descrição 2", usuario, mutableListOf(), mutableListOf())

        `when`(userRepository.findById(usuario.id!!)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.findAllByCriadoPor(usuario)).thenReturn(listOf(board1))
        `when`(boardRepository.findAllByUsuariosContains(usuario)).thenReturn(listOf(board2))

        val (boardsCriados, boardsAssociados) = boardService.buscarBoards(usuario.id!!)

        assertEquals(1, boardsCriados.size)
        assertEquals(board1.nome, boardsCriados[0].nome)
        assertEquals(1, boardsAssociados.size)
        assertEquals(board2.nome, boardsAssociados[0].nome)
    }

    @Test
    fun `deve lançar exceção ao tentar buscar boards com usuário inexistente`() {
        `when`(userRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            boardService.buscarBoards(1)
        }

        assertEquals("Usuário não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve deletar board com sucesso`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())

        `when`(boardRepository.findById(1)).thenReturn(Optional.of(board))

        boardService.deletarBoard(1)

        verify(taskRepository, times(1)).deleteAllByBoardId(1)
        verify(boardRepository, times(1)).delete(board)
    }

    @Test
    fun `deve lançar exceção ao tentar deletar board inexistente`() {
        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            boardService.deletarBoard(1)
        }

        assertEquals("Board não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve visualizar board com sucesso quando usuario e criador`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board Test", "Descrição do Board", usuario, mutableListOf(usuario), mutableListOf())

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.findById(1)).thenReturn(Optional.of(board))

        val result = boardService.visualizarBoard(1, 1)

        assertNotNull(result)
    }

    @Test
    fun `deve visualizar board com sucesso quando usuario esta associado`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val boardDono = User(1, "usuarioDono", "testeDono@example.com", "senha123")
        val board = Board(1, "Board Test", "Descrição do Board", boardDono, mutableListOf(usuario), mutableListOf())

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.findById(1)).thenReturn(Optional.of(board))

        val result = boardService.visualizarBoard(1, 1)

        assertNotNull(result)
    }

    @Test
    fun `deve retornar null quando usuario nao e criador nem associado`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val boardDono = User(1, "usuarioDono", "testeDono@example.com", "senha123")
        val board = Board(1, "Board Test", "Descrição do Board", boardDono, mutableListOf(), mutableListOf())

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.findById(1)).thenReturn(Optional.of(board))

        val result = boardService.visualizarBoard(1, 1)

        assertNull(result)
    }

    @Test
    fun `deve lançar exceção quando usuario nao encontrado`() {
        `when`(userRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            boardService.visualizarBoard(1, 1)
        }

        assertEquals("Usuário não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve lançar exceção quando board nao encontrado`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")

        `when`(userRepository.findById(1)).thenReturn(Optional.of(usuario))
        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            boardService.visualizarBoard(1, 1)
        }

        assertEquals("Board não encontrado com o ID 1", exception.message)
    }
}
