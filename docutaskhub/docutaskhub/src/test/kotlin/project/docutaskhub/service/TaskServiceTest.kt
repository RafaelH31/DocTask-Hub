import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.Task
import project.docutaskhub.dominio.User
import project.docutaskhub.dto.TaskRequest
import project.docutaskhub.enums.Status
import project.docutaskhub.repository.*
import project.docutaskhub.service.DocumentService
import project.docutaskhub.service.TaskService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class TaskServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val taskRepository: TaskRepository = mock(TaskRepository::class.java)
    private val documentRepository: DocumentRepository = mock(DocumentRepository::class.java)
    private val boardRepository: BoardRepository = mock(BoardRepository::class.java)
    private val taskService: TaskService = TaskService(boardRepository,taskRepository,userRepository,documentRepository)


    @Test
    fun `deve criar uma nova tarefa com sucesso`() {
        val usuario =  User(1, "donoBoard", "dono@example.com", "senha123")
        val board = Board(1, "Board Teste", "Descrição do Board", usuario, mutableListOf(), mutableListOf())
        val criadoPor = User(1, "usuarioCriador", "criador@example.com", "senha123")
        val atribuidoPara = User(2, "usuarioAtribuido", "atribuido@example.com", "senha123")

        val taskRequest = TaskRequest(
            titulo = "Nova Tarefa",
            descricao = "Descrição da Tarefa",
            status = Status.DOING,
            cor = "Azul",
            criadoPorId = criadoPor.id!!,
            atribuidoParaId = atribuidoPara.id!!,
            dataDeVencimento = LocalDate.now().plusDays(1),
            dataDeCriacao = LocalDateTime.now()
        )

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findById(criadoPor.id!!)).thenReturn(Optional.of(criadoPor))
        `when`(userRepository.findById(atribuidoPara.id!!)).thenReturn(Optional.of(atribuidoPara))
        `when`(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        val savedTask = taskService.criarTask(board.id!!, taskRequest)

        assertNotNull(savedTask)
        assertEquals(taskRequest.titulo, (savedTask as Task).titulo)
    }

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
    fun `deve visualizar tarefa com sucesso`() {
        val usuario =  User(1, "donoBoard", "dono@example.com", "senha123")
        val board = Board(1, "Board Teste", "Descrição do Board", usuario, mutableListOf(), mutableListOf())
        val criadoPor = User(2, "usuarioCriador", "criador@example.com", "senha123")
        val atribuidoPara = User(3, "usuarioAtribuido", "atribuido@example.com", "senha123")
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DONE, board, criadoPor,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), atribuidoPara, mutableListOf()
        )
        board.tasks = mutableListOf(task)

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findById(criadoPor.id!!)).thenReturn(Optional.of(criadoPor))
        `when`(userRepository.findById(atribuidoPara.id!!)).thenReturn(Optional.of(atribuidoPara))

        val taskResponse = taskService.visualizarTask(board.id!!, task.id!!)

        assertNotNull(taskResponse)
        assertEquals(task.titulo, taskResponse.titulo)
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
    fun `deve atualizar tarefa com sucesso`() {
        val criadoPor = User(1, "usuarioCriador", "criador@example.com", "senha123")
        val board = Board(1, "Board Teste", "Descrição do Board", criadoPor, mutableListOf(), mutableListOf())
        val atribuidoPara = User(2, "usuarioAtribuido", "atribuido@example.com", "senha123")
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DONE, board, criadoPor,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), atribuidoPara, mutableListOf()
        )
        board.tasks = mutableListOf(task)

        val taskRequest = TaskRequest(
            titulo = "Tarefa Atualizada",
            descricao = "Descrição Atualizada",
            status = Status.DONE,
            cor = "Verde",
            criadoPorId = criadoPor.id!!,
            atribuidoParaId = atribuidoPara.id!!,
            dataDeVencimento = LocalDate.now().plusDays(1),
            dataDeCriacao = LocalDateTime.now()
        )

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findById(criadoPor.id!!)).thenReturn(Optional.of(criadoPor))
        `when`(userRepository.findById(atribuidoPara.id!!)).thenReturn(Optional.of(atribuidoPara))
        `when`(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        val updatedTask = taskService.updateTask(board.id!!, task.id!!, taskRequest)

        assertNotNull(updatedTask)
        assertEquals(taskRequest.titulo, updatedTask.titulo)
        assertEquals(taskRequest.status, updatedTask.status)
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
        val board = Board(1, "Board Teste", "Descrição do Board", user, mutableListOf(), mutableListOf())
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DOING, board, user,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), user, mutableListOf()
        )
        board.tasks = mutableListOf(task)

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))

        taskService.deleteTask(user.id!!, board.id!!, task.id!!)

        verify(documentRepository, times(1)).deleteAll(task.documentos)
        verify(taskRepository, times(1)).delete(task)
    }

    @Test
    fun `deve lançar exceção ao tentar deletar tarefa com quadro inexistente`() {
        `when`(boardRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            taskService.deleteTask(1, 1, 1)
        }

        assertEquals("Quadro não encontrado com o ID 1", exception.message)
    }
}
