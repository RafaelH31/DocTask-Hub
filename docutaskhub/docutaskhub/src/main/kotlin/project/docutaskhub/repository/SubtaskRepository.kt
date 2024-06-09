package project.docutaskhub.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import project.docutaskhub.dominio.Subtask

@Repository
interface SubtaskRepository : JpaRepository<Subtask, Int> {
}