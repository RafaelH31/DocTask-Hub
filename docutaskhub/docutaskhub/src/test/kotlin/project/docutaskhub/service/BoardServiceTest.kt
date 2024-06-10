import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.Document
import project.docutaskhub.dominio.Task
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.BoardAttRequest
import project.docutaskhub.dto.BoardRequest
import project.docutaskhub.enums.DocumentType
import project.docutaskhub.enums.Status
import project.docutaskhub.repository.BoardRepository
import project.docutaskhub.repository.DocumentRepository
import project.docutaskhub.repository.TaskRepository
import project.docutaskhub.repository.UserRepository
import project.docutaskhub.service.BoardService
import project.docutaskhub.service.TaskService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class BoardServiceTest {

    private val taskRepository: TaskRepository = mock(TaskRepository::class.java)
    private val boardRepository: BoardRepository = mock(BoardRepository::class.java)
    private val documentRepository: DocumentRepository = mock(DocumentRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val taskService: TaskService = TaskService(boardRepository, taskRepository, userRepository, documentRepository)
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

    @Test
    fun `deve retornar todas as tarefas de um board`() {
        val user1 = User(1, "usuarioTeste1", "teste1@example.com", "senha123")
        val user2 = User(2, "usuarioTeste2", "teste2@example.com", "senha456")
        val board = Board(1, "Board Teste", "Descrição do Board", user1, mutableListOf(user1, user2), mutableListOf())
        val document = Document(1, "Documento Teste", ByteArray(0), DocumentType.PDF, null)
        val task1 = Task(
            1, "Tarefa Teste 1", "Descrição da Tarefa 1", Status.DOING, board, user1,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), user1, mutableListOf(document)
        )
        val task2 = Task(
            2, "Tarefa Teste 2", "Descrição da Tarefa 2", Status.DOING, board, user2,
            "Vermelho", LocalDateTime.now().plusDays(2), LocalDateTime.now(), LocalDate.now(), user2, mutableListOf()
        )
        board.tasks = mutableListOf(task1, task2)
        document.task = task1

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findById(user1.id!!)).thenReturn(Optional.of(user1))
        `when`(userRepository.findById(user2.id!!)).thenReturn(Optional.of(user2))
        `when`(userRepository.findById(task1.criadoPor.id!!)).thenReturn(Optional.of(user1))
        `when`(userRepository.findById(task1.atribuidoPara.id!!)).thenReturn(Optional.of(user1))
        `when`(userRepository.findById(task2.criadoPor.id!!)).thenReturn(Optional.of(user2))
        `when`(userRepository.findById(task2.atribuidoPara.id!!)).thenReturn(Optional.of(user2))

        val tasks = taskService.getAllTasksFromBoard(board.id!!)

        assertNotNull(tasks)
        assertEquals(2, tasks.size)

        val taskResponse1 = tasks.find { it.id == task1.id }
        assertNotNull(taskResponse1)
        assertEquals(task1.titulo, taskResponse1?.titulo)
        assertEquals(task1.descricao, taskResponse1?.descricao)
        assertEquals(task1.status, taskResponse1?.status)
        assertEquals(task1.cor, taskResponse1?.cor)
        assertEquals(task1.dataDeCriacao, taskResponse1?.dataDeCriacao)
        assertEquals(task1.dataDeAtualizacao, taskResponse1?.dataDeAtualizacao)
        assertEquals(task1.dataDeVencimento, taskResponse1?.dataDeVencimento)
        assertEquals(user1.id, taskResponse1?.criadoPorId?.id)
        assertEquals(user1.id, taskResponse1?.atribuidoParaId?.id)
        assertEquals(1, taskResponse1?.documentos?.size)
        assertEquals(document.id, taskResponse1?.documentos?.get(0)?.id)
        assertEquals(document.nome, taskResponse1?.documentos?.get(0)?.nome)
        assertEquals(document.type, taskResponse1?.documentos?.get(0)?.type)
        assertEquals(task1.id, taskResponse1?.documentos?.get(0)?.taskId)

        val taskResponse2 = tasks.find { it.id == task2.id }
        assertNotNull(taskResponse2)
        assertEquals(task2.titulo, taskResponse2?.titulo)
        assertEquals(task2.descricao, taskResponse2?.descricao)
        assertEquals(task2.status, taskResponse2?.status)
        assertEquals(task2.cor, taskResponse2?.cor)
        assertEquals(task2.dataDeCriacao, taskResponse2?.dataDeCriacao)
        assertEquals(task2.dataDeAtualizacao, taskResponse2?.dataDeAtualizacao)
        assertEquals(task2.dataDeVencimento, taskResponse2?.dataDeVencimento)
        assertEquals(user2.id, taskResponse2?.criadoPorId?.id)
        assertEquals(user2.id, taskResponse2?.atribuidoParaId?.id)
        assertEquals(0, taskResponse2?.documentos?.size)
    }

    @Test
    fun `deve atualizar o board com sucesso`() {
        val user1 = User(1, "usuarioTeste1", "teste1@example.com", "senha123")
        val user2 = User(2, "usuarioTeste2", "teste2@example.com", "senha456")
        val board = Board(1, "Board Teste", "Descrição do Board", user1, mutableListOf(user1), mutableListOf())

        val updateRequest = BoardAttRequest(
            nome = "Board Atualizado",
            descricao = "Descrição Atualizada",
            usuarios = listOf("teste1@example.com", "teste2@example.com")
        )

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findByEmailIn(updateRequest.usuarios!!)).thenReturn(listOf(user1, user2))
        `when`(boardRepository.save(any(Board::class.java))).thenReturn(board)

        val response = boardService.atualizarBoard(board.id!!, updateRequest)

        assertNotNull(response)
        assertEquals(updateRequest.nome, response.nome)
        assertEquals(updateRequest.descricao, response.descricao)
        assertEquals(updateRequest.usuarios!!.size, response.usuarios!!.size)
        assertTrue(response.usuarios!!.contains(user1.email))
        assertTrue(response.usuarios!!.contains(user2.email))

        verify(boardRepository, times(1)).findById(board.id!!)
        verify(userRepository, times(1)).findByEmailIn(updateRequest.usuarios!!)
        verify(boardRepository, times(1)).save(any(Board::class.java))
    }

}
