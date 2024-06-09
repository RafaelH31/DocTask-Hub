package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.Task

@Repository
interface TaskRepository : JpaRepository<Task, Int>{

    fun deleteAllByBoardId(boardId: Int)

}