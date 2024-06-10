package project.docutaskhub.service

import org.springframework.stereotype.Service
import project.docutaskhub.dominio.Task
import project.docutaskhub.dto.DocumentResponse
import project.docutaskhub.dto.TaskRequest
import project.docutaskhub.dto.TaskResponse
import project.docutaskhub.repository.*
import java.time.LocalDateTime


@Service
class TaskService (
    private val boardRepository: BoardRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository,
)

{
    fun criarTask(boardId: Int, taskRequest: TaskRequest): Any {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        val atribuidoPara = userRepository.findById(taskRequest.atribuidoParaId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${taskRequest.atribuidoParaId}") }

        val criadoPor = userRepository.findById(taskRequest.criadoPorId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${taskRequest.criadoPorId}") }

        val task = Task(
            titulo = taskRequest.titulo,
            descricao = taskRequest.descricao,
            status = taskRequest.status,
            cor = taskRequest.cor,
            atribuidoPara = atribuidoPara,
            criadoPor = criadoPor,
            dataDeVencimento = taskRequest.dataDeVencimento,
            dataDeCriacao = taskRequest.dataDeCriacao,
            board = board
        )

        val savedTask = taskRepository.save(task)
        return savedTask

    }

    fun visualizarTask(boardId: Int, taskId: Int): TaskResponse {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        val task = board.tasks?.find { it.id == taskId }
            ?: throw IllegalArgumentException("Tarefa não encontrada com o ID $taskId no quadro com o ID $boardId")

        val atribuidoPara = userRepository.findById(task.atribuidoPara.id!!)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.atribuidoPara.id}") }

        val criadoPor = userRepository.findById(task.criadoPor.id!!)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.criadoPor.id!!}") }


        val documentos = task.documentos.map { documento ->
            DocumentResponse(
                id = documento.id,
                nome = documento.nome,
                type = documento.type,
                taskId = documento.task!!.id
            )
        }

        return TaskResponse(
            id = task.id!!,
            titulo = task.titulo,
            descricao = task.descricao,
            status = task.status,
            cor = task.cor,
            dataDeCriacao = task.dataDeCriacao,
            dataDeAtualizacao = task.dataDeAtualizacao,
            dataDeVencimento = task.dataDeVencimento,
            criadoPorId = criadoPor,
            atribuidoParaId = atribuidoPara,
            documentos = documentos
        )
    }

    fun getAllTasksFromBoard(boardId: Int): List<TaskResponse> {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        val tasks = board.tasks ?: emptyList()

        return tasks.map { task ->
            val criadoPor = userRepository.findById(task.criadoPor.id!!)
                .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.criadoPor.id!!}") }

            val atribuidoPara = userRepository.findById(task.atribuidoPara.id!!)
                .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.atribuidoPara.id}") }


            val documentos = task.documentos.map { documento ->
                DocumentResponse(
                    id = documento.id,
                    nome = documento.nome,
                    type = documento.type,
                    taskId = documento.task!!.id
                )
            }

            TaskResponse(
                id = task.id!!,
                titulo = task.titulo,
                descricao = task.descricao,
                status = task.status,
                cor = task.cor,
                dataDeCriacao = task.dataDeCriacao,
                dataDeAtualizacao = task.dataDeAtualizacao,
                dataDeVencimento = task.dataDeVencimento,
                criadoPorId = criadoPor,
                atribuidoParaId = atribuidoPara,
                documentos = documentos
            )
        }
    }

    fun getTasksByBoardAndUser(boardId: Int, userId: Int): List<TaskResponse> {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID $userId") }

        val tasks = board.tasks ?: emptyList()

        return tasks.filter { it.atribuidoPara.id == userId }.map { task ->
            val criadoPor = userRepository.findById(task.criadoPor.id!!)
                .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.criadoPor.id!!}") }

            val atribuidoPara = userRepository.findById(task.atribuidoPara.id!!)
                .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.atribuidoPara.id}") }


            val documentos = task.documentos.map { documento ->
                DocumentResponse(
                    id = documento.id,
                    nome = documento.nome,
                    type = documento.type,
                    taskId = documento.task!!.id
                )
            }

            TaskResponse(
                id = task.id!!,
                titulo = task.titulo,
                descricao = task.descricao,
                status = task.status,
                cor = task.cor,
                dataDeCriacao = task.dataDeCriacao,
                dataDeAtualizacao = task.dataDeAtualizacao,
                dataDeVencimento = task.dataDeVencimento,
                criadoPorId = criadoPor,
                atribuidoParaId = atribuidoPara,
                documentos = documentos
            )
        }
    }

    fun updateTask(boardId: Int, taskId: Int, taskRequest: TaskRequest): TaskResponse {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        val task = board.tasks?.find { it.id == taskId }
            ?: throw IllegalArgumentException("Tarefa não encontrada com o ID $taskId no quadro com o ID $boardId")

        val criadoPor = userRepository.findById(task.criadoPor.id!!)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.criadoPor.id!!}") }

        val atribuidoPara = userRepository.findById(task.atribuidoPara.id!!)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado com o ID ${task.atribuidoPara.id}") }

        task.apply {
            taskRequest.titulo?.let { this.titulo = it }
            taskRequest.descricao?.let { this.descricao = it }
            taskRequest.cor?.let { this.cor = it }
            taskRequest.dataDeVencimento?.let { this.dataDeVencimento = it }
            this.dataDeAtualizacao = LocalDateTime.now()
        }

        val savedTask = taskRepository.save(task)

        return TaskResponse(
            id = savedTask.id!!,
            titulo = savedTask.titulo,
            descricao = savedTask.descricao,
            status = savedTask.status,
            cor = savedTask.cor,
            dataDeCriacao = savedTask.dataDeCriacao,
            dataDeAtualizacao = savedTask.dataDeAtualizacao,
            dataDeVencimento = savedTask.dataDeVencimento,
            criadoPorId = criadoPor,
            atribuidoParaId = atribuidoPara,
            documentos = savedTask.documentos.map { documento ->
                DocumentResponse(
                    id = documento.id,
                    nome = documento.nome,
                    type = documento.type,
                    taskId = documento.task!!.id
                )
            }
        )
    }

    fun deleteTask(userId: Int, boardId: Int, taskId: Int) {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Quadro não encontrado com o ID $boardId") }

        if (board.criadoPor.id != userId) {
            val isAssociated = board.usuarios?.any { it.id == userId } ?: false
            if (!isAssociated) {
                throw IllegalAccessException("Usuário não tem permissão para excluir tarefas deste quadro")
            }
        }

        val task = board.tasks?.find { it.id == taskId }
            ?: throw IllegalArgumentException("Tarefa não encontrada com o ID $taskId no quadro com o ID $boardId")

        task.documentos.forEach { documentRepository.delete(it) }

        taskRepository.delete(task)
    }

}