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

    @Test
    fun `deve lançar exceção quando tarefa não encontrada`() {
        val user = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board Teste", "Descrição do Board", user, mutableListOf(user), mutableListOf())

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))

        val exception = assertThrows<IllegalArgumentException> {
            taskService.deleteTask(user.id!!, board.id!!, 99)
        }

        assertEquals("Tarefa não encontrada com o ID 99 no quadro com o ID 1", exception.message)
    }

    @Test
    fun `deve lançar exceção quando usuário não tem permissão`() {
        val user = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val anotherUser = User(2, "outroUsuario", "outro@example.com", "senha456")
        val board = Board(1, "Board Teste", "Descrição do Board", anotherUser, mutableListOf(), mutableListOf())

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))

        val exception = assertThrows<IllegalAccessException> {
            taskService.deleteTask(user.id!!, board.id!!, 1)
        }

        assertEquals("Usuário não tem permissão para excluir tarefas deste quadro", exception.message)
    }

    @Test
    fun `deve retornar as tarefas atribuídas ao usuário no quadro especificado`() {
        val user = User(1, "usuarioTeste", "teste@example.com", "sen" +
                "ha123")

        val outroUser = User(2, "outroUsuario", "outro@example.com", "senha456")
        val task1 = Task(
            1, "Tarefa 1", "Descrição da Tarefa 1", Status.DOING, null, user,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), user, mutableListOf()
        )
        val task2 = Task(
            2, "Tarefa 2", "Descrição da Tarefa 2", Status.DONE, null, outroUser,
            "Verde", LocalDateTime.now().plusDays(2), LocalDateTime.now(), LocalDate.now(), outroUser, mutableListOf()
        )
        val board = Board(1, "Board Teste", "Descrição do Board", user, mutableListOf(user), mutableListOf(task1, task2))

        `when`(boardRepository.findById(board.id!!)).thenReturn(Optional.of(board))
        `when`(userRepository.findById(user.id!!)).thenReturn(Optional.of(user))
        `when`(userRepository.findById(task1.criadoPor.id!!)).thenReturn(Optional.of(user))
        `when`(userRepository.findById(task1.atribuidoPara.id!!)).thenReturn(Optional.of(user))

        val tasks = taskService.getTasksByBoardAndUser(board.id!!, user.id!!)

        assertNotNull(tasks)
        assertEquals(1, tasks.size)
        assertEquals(task1.id, tasks[0].id)
        assertEquals(task1.titulo, tasks[0].titulo)
        assertEquals(task1.descricao, tasks[0].descricao)
        assertEquals(task1.status, tasks[0].status)
        assertEquals(task1.cor, tasks[0].cor)
        assertEquals(task1.dataDeCriacao, tasks[0].dataDeCriacao)
        assertEquals(task1.dataDeAtualizacao, tasks[0].dataDeAtualizacao)
        assertEquals(task1.dataDeVencimento, tasks[0].dataDeVencimento)
        assertEquals(task1.criadoPor, tasks[0].criadoPorId)
        assertEquals(task1.atribuidoPara, tasks[0].atribuidoParaId)
        assertTrue(tasks[0].documentos.isEmpty())

        verify(boardRepository, times(1)).findById(board.id!!)
    }
}
