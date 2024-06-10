import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import project.docutaskhub.dominio.Board
import project.docutaskhub.dominio.Document
import project.docutaskhub.dominio.Task
import project.docutaskhub.dominio.User
import project.docutaskhub.enums.DocumentType
import project.docutaskhub.enums.Status
import project.docutaskhub.repository.DocumentRepository
import project.docutaskhub.repository.TaskRepository
import project.docutaskhub.service.DocumentService
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DocumentServiceTest {

    private val documentRepository: DocumentRepository = mock(DocumentRepository::class.java)
    private val taskRepository: TaskRepository = mock(TaskRepository::class.java)
    private val documentService: DocumentService = DocumentService(documentRepository, taskRepository)

    @Test
    fun `deve enviar documento com sucesso`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())

        val task = Task(1, "Tarefa Teste", "Descrição da Tarefa", Status.DOING, board, usuario, "Azul",
            LocalDateTime.now(), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf())
        val file = MockMultipartFile("file", "test.txt", "text/plain", "Test content".toByteArray())
        val documentType = DocumentType.TXT

        `when`(taskRepository.findById(task.id!!)).thenReturn(Optional.of(task))
        `when`(documentRepository.save(any(Document::class.java))).thenAnswer { it.arguments[0] }

        val savedDocument = documentService.enviarDocumento(task.id!!, file, documentType)

        assertNotNull(savedDocument)
        assertEquals(file.originalFilename, savedDocument.nome)
        assertArrayEquals(file.bytes, savedDocument.arquivo)
    }

    @Test
    fun `deve lançar exceção ao tentar enviar documento para tarefa inexistente`() {
        val file = MockMultipartFile("file", "test.txt", "text/plain", "Test content".toByteArray())
        val documentType = DocumentType.TXT

        `when`(taskRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            documentService.enviarDocumento(1, file, documentType)
        }

        assertEquals("Tarefa não encontrada com o ID 1", exception.message)
    }

    @Test
    fun `deve lançar exceção ao tentar enviar documento vazio`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())

        val task = Task(1, "Tarefa Teste", "Descrição da Tarefa", Status.DOING, board, usuario, "Azul",
            LocalDateTime.now(), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf())
        val file = MockMultipartFile("file", "empty.txt", "text/plain", ByteArray(0))
        val documentType = DocumentType.TXT

        `when`(taskRepository.findById(task.id!!)).thenReturn(Optional.of(task))

        val exception = assertThrows<IllegalArgumentException> {
            documentService.enviarDocumento(task.id!!, file, documentType)
        }

        assertEquals("Arquivo vazio não é permitido", exception.message)
    }

    @Test
    fun `deve lançar exceção ao tentar enviar documento com falha ao ler o arquivo`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())

        val task = Task(1, "Tarefa Teste", "Descrição da Tarefa", Status.DOING, board, usuario, "Azul",
            LocalDateTime.now(), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf())

        val file = mock(MultipartFile::class.java)
        val documentType = DocumentType.TXT

        `when`(taskRepository.findById(task.id!!)).thenReturn(Optional.of(task))
        `when`(file.isEmpty).thenReturn(false)
        `when`(file.bytes).thenThrow(IOException::class.java)

        val exception = assertThrows<IllegalArgumentException> {
            documentService.enviarDocumento(task.id!!, file, documentType)
        }

        assertEquals("Falha ao ler o arquivo", exception.message)
    }

    @Test
    fun `deve buscar tipo de documento corretamente`() {
        val imageFile = MockMultipartFile("file", "image.jpg", "image/jpeg", ByteArray(10))
        val pdfFile = MockMultipartFile("file", "file.pdf", "application/pdf", ByteArray(10))
        val textFile = MockMultipartFile("file", "file.txt", "text/plain", ByteArray(10))
        val csvFile = MockMultipartFile("file", "file.csv", "text/csv", ByteArray(10))
        val otherFile = MockMultipartFile("file", "file.bin", "application/octet-stream", ByteArray(10))

        assertEquals(DocumentType.IMAGE, documentService.buscarTipo(imageFile))
        assertEquals(DocumentType.PDF, documentService.buscarTipo(pdfFile))
        assertEquals(DocumentType.TXT, documentService.buscarTipo(textFile))
        assertEquals(DocumentType.CSV, documentService.buscarTipo(csvFile))
        assertEquals(DocumentType.OTHER, documentService.buscarTipo(otherFile))
    }

    @Test
    fun `deve listar todos os documentos do board`() {

        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DONE, board, usuario,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf()
        )

        val document1 = Document(1, "doc1.txt", ByteArray(10), DocumentType.TXT, task)
        val document2 = Document(2, "doc2.pdf", ByteArray(10), DocumentType.PDF, task)

        `when`(documentRepository.findAllByTaskBoardId(1)).thenReturn(listOf(document1, document2))

        val documents = documentService.listarTodosOsDocumentos(1)

        assertEquals(2, documents.size)
        assertEquals(document1.nome, documents[0].nome)
        assertEquals(document2.nome, documents[1].nome)
    }

    @Test
    fun `deve obter documento por id e board id`() {

        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DONE, board, usuario,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf()
        )

        val document = Document(1, "doc1.txt", ByteArray(10), DocumentType.TXT, task)

        `when`(documentRepository.findByIdAndTaskBoardId(1, 1)).thenReturn(Optional.of(document))

        val foundDocument = documentService.getDocument(1, 1)

        assertNotNull(foundDocument)
        assertEquals(document.nome, foundDocument.nome)
    }

    @Test
    fun `deve lançar exceção ao tentar obter documento inexistente por id e board id`() {
        `when`(documentRepository.findByIdAndTaskBoardId(1, 1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            documentService.getDocument(1, 1)
        }

        assertEquals("Documento não encontrado com ID 1 no quadro com ID 1", exception.message)
    }

    @Test
    fun `deve deletar documento com sucesso`() {
        val usuario = User(1, "usuarioTeste", "teste@example.com", "senha123")
        val board = Board(1, "Board 1", "Descrição 1", usuario, mutableListOf(), mutableListOf())
        val task = Task(
            1, "Tarefa Teste", "Descrição da Tarefa", Status.DONE, board, usuario,
            "Azul", LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDate.now(), usuario, mutableListOf()
        )
        val document = Document(1, "doc1.txt", ByteArray(10), DocumentType.TXT, task)

        `when`(documentRepository.findByIdAndTaskBoardId(1, 1)).thenReturn(Optional.of(document))

        documentService.deletarDocumento(1, 1)

        verify(documentRepository, times(1)).delete(document)
    }

    @Test
    fun `deve lançar exceção ao tentar deletar documento inexistente`() {
        `when`(documentRepository.findByIdAndTaskBoardId(1, 1)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            documentService.deletarDocumento(1, 1)
        }

        assertEquals("Documento não encontrado com o ID 1 no quadro com o ID 1", exception.message)
    }
}
