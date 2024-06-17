import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.Document
import project.docutaskhub.dominio.Task
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.TaskRequest
import project.docutaskhub.enums.DocumentType
import project.docutaskhub.enums.Status
import project.docutaskhub.repository.*
import project.docutaskhub.service.TaskService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TaskServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val taskRepository: TaskRepository = mock(TaskRepository::class.java)
    private val documentRepository: DocumentRepository = mock(DocumentRepository::class.java)
    private val boardRepository: BoardRepository = mock(BoardRepository::class.java)
    private val taskService: TaskService = TaskService(boardRepository, taskRepository, userRepository, documentRepository)

    @Test
    fun `deve lançar exceção ao tentar criar uma tarefa com quadro inexistente`() {
        val taskRequest = TaskRequest(
            titulo = "Nova Tarefa",
            descricao = "Descrição da Tarefa",
            status = Status.DELAYED,
            cor = "Azul",
            criadoPorId = 1,
            atribuidoParaId = 2,
            dataDeVencimento = LocalDate.now().plusDays(1),
            dataDeCriacao = LocalDateTime.now()
        )

        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            taskService.criarTask(1, taskRequest)
        }

        assertEquals("Quadro não encontrado com o ID 1", exception.message)
    }


    @Test
    fun `deve lançar exceção ao tentar visualizar tarefa com quadro inexistente`() {
        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            taskService.visualizarTask(1, 1)
        }

        assertEquals("Quadro não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve lançar exceção ao tentar atualizar tarefa com quadro inexistente`() {
        val taskRequest = TaskRequest(
            titulo = "Tarefa Atualizada",
            descricao = "Descrição Atualizada",
            status = Status.DELAYED,
            cor = "Verde",
            criadoPorId = 1,
            atribuidoParaId = 2,
            dataDeVencimento = LocalDate.now().plusDays(1),
            dataDeCriacao = LocalDateTime.now()
        )

        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            taskService.updateTask(1, 1, taskRequest)
        }

        assertEquals("Quadro não encontrado com o ID 1", exception.message)
    }

    @Test
    fun `deve deletar tarefa com sucesso`() {
        val user = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board Teste", "Descrição do Board", user, mutableListOf(user), mutableListOf())
        val document = Document(1, "Documento Teste", ByteArray(0), DocumentType.PDF, null)
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DOING, board, user,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), user, mutableListOf(document)
        )
        board.tasks = mutableListOf(task)
        document.task = task

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))

        taskService.deleteTask(user.id!!, board.id!!, task.id!!)

        verify(documentRepository, times(1)).delete(document)
        verify(taskRepository, times(1)).delete(task)
    }

    @Test
    fun `deve lançar exceção quando quadro não encontrado`() {
        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            taskService.deleteTask(1, 1, 1)
        }

        assertEquals("Quadro não encontrado com o ID 1", exception.message)
    }



}
