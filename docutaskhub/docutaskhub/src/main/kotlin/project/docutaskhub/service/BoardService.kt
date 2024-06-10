package project.docutaskhub.service

import org.springframework.stereotype.Service
import project.docutaskhub.dominio.Board
import project.docutaskhub.dto.*
import project.docutaskhub.repository.BoardRepository
import project.docutaskhub.repository.TaskRepository
import project.docutaskhub.repository.UserRepository
import java.time.LocalDateTime


@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) {

    fun criarBoard(boardRequest: BoardRequest): BoardResponse {
        val criadoPor = userRepository.findById(boardRequest.fkUsuario)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${boardRequest.fkUsuario}") }

        val novoBoard = Board(
            nome = boardRequest.nome,
            descricao = boardRequest.descricao,
            criadoPor = criadoPor,
            usuarios = emptyList(),
            tasks = emptyList()
        )

        val savedBoard = boardRepository.save(novoBoard)

        return BoardResponse(
            nome = savedBoard.nome,
            descricao = savedBoard.descricao,
            criadoPorId = savedBoard.criadoPor.id!!,
            usuarios = emptyList(),
            tasks = emptyList()
        )
    }

    fun buscarBoards(userId: Int): Pair<List<BoardResponse>, List<BoardResponse>> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID $userId") }

        val boardsCriados = boardRepository.findAllByCriadoPor(user)
            .map {
                BoardResponse(
                    id = it.id!!,
                    nome = it.nome,
                    descricao = it.descricao,
                    criadoPorId = it.criadoPor.id!!,
                    usuarios = it.usuarios?.map { user ->
                        UserResponse(
                            id = user.id!!,
                            username = user.username,
                            email = user.email,
                            dataDeRegistro = user.dataDeRegistro
                        )
                    },
                    tasks = null
                )
            }

        val boardsAssociados = boardRepository.findAllByUsuariosContains(user)
            .map {
                BoardResponse(
                    id = it.id!!,
                    nome = it.nome,
                    descricao = it.descricao,
                    criadoPorId = it.criadoPor.id!!,
                    usuarios = it.usuarios?.map { user ->
                        UserResponse(
                            id = user.id!!,
                            username = user.username,
                            email = user.email,
                            dataDeRegistro = user.dataDeRegistro
                        )
                    },
                    tasks = null
                )
            }

        return Pair(boardsCriados, boardsAssociados)
    }

    fun visualizarBoard(userId: Int, boardId: Int): List<Any>? {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID $userId") }

        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board não encontrado com o ID $boardId") }

        val isCreator = user == board.criadoPor
        val isAssociated = board.usuarios?.contains(user)

        return if (isCreator || isAssociated == true) {
            val tasks = board.tasks!!.map { task ->
                TaskResponse(
                    id = task.id!!,
                    titulo = task.titulo,
                    descricao = task.descricao,
                    status = task.status,
                    cor = task.cor,
                    dataDeCriacao = task.dataDeCriacao,
                    dataDeAtualizacao = task.dataDeAtualizacao,
                    dataDeVencimento = task.dataDeVencimento,
                    criadoPorId = task.criadoPor,
                    atribuidoParaId = task.atribuidoPara,
                    documentos = task.documentos.map { document ->
                        DocumentResponse(
                            id = document.id,
                            nome = document.nome,
                            type = document.type,
                            taskId = task.id!!
                        )
                    }
                )
            }
            tasks
        } else {
            null
        }
    }


    fun atualizarBoard(boardId: Int, updateRequest: BoardAttRequest): BoardResponseAtt {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board não encontrado com o ID $boardId") }

        updateRequest.nome?.let { board.nome = it }
        updateRequest.descricao?.let { board.descricao = it }

        updateRequest.usuarios?.let { userEmails ->
            val users = userRepository.findByEmailIn(userEmails)
            board.usuarios = users.toMutableList()
        }

        val updatedBoard = boardRepository.save(board)

        return BoardResponseAtt(
            id = updatedBoard.id,
            nome = updatedBoard.nome,
            descricao = updatedBoard.descricao,
            usuarios = updatedBoard.usuarios?.map { it.email }
        )
    }

    fun deletarBoard(boardId: Int) {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board não encontrado com o ID $boardId") }

        taskRepository.deleteAllByBoardId(boardId)
        boardRepository.delete(board)
    }



}
